package com.lbwwz.easyrabbitmq;

import com.lbwwz.easyrabbitmq.util.MqBizUtil;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import com.alibaba.fastjson.JSONObject;

/**
 * @author lbwwz
 */
public class QueueServiceImpl extends AbstractBrokerManager implements QueueService {

    private final static int LISTEN_THREAD_COUNT = 3;

    private static final Logger LOGGER = LoggerFactory.getLogger(QueueServiceImpl.class);

    private Channel getChannel() throws IOException, TimeoutException {
        Connection connection = connectionFactory.newConnection();
        return connection.createChannel();

    }

    public <T> void consume(String msgTitle,
                            String consumerTag,
                            int threadCount,
                            Class<T> clazz,
                            Consumer<T> msgHandler) {

        consume(msgTitle,consumerTag,null,threadCount,clazz,msgHandler);
    }

    @Override
    public <T> void consume(String msgTitle,
                            String consumerTag,
                            Map<String, Object> arguments,
                            int threadCount,
                            Class<T> clazz,
                            Consumer<T> msgHandler) {
        String queueName = MqBizUtil.makeQueueName(msgTitle);
        //定义队列和绑定队列到exchange

        // 定义停机信号
        AtomicBoolean isStopping = new AtomicBoolean();
        // 多线程支持
        List<Thread> threadList = new ArrayList<>();
        for (int i = 1; i <= threadCount;i++) {
            Thread thread = new Thread(
                new ConsumeMassageHandle<>(queueName, isStopping, consumerTag + i, arguments, clazz, msgHandler));
            thread.start();
            threadList.add(thread);
        }
        addShutDownHookForConsumer(queueName, isStopping, threadList);
    }

    public <T> void consume(String queue,
                            int threadCount,
                            Class<T> clazz,
                            Consumer<T> msgHandler) {

        // 定义停机信号
        AtomicBoolean isStopping = new AtomicBoolean();
        // 多线程支持
        List<Thread> threadList = new ArrayList<>();
        for (int i = 1; i <= threadCount; ++i) {
            Thread thread = new Thread(
                new ConsumeMassageHandle<>(queue, isStopping, i + "", null, clazz, msgHandler));
            thread.start();
            threadList.add(thread);
        }
        addShutDownHookForConsumer(queue, isStopping, threadList);

    }

    /**
     * @param queue      队列名称
     * @param isStopping 任务结束标的标记
     * @param threadList 需要等待释放操作的线程
     */
    private void addShutDownHookForConsumer(String queue, AtomicBoolean isStopping, List<Thread> threadList) {
        // 监听系统停机信号
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("停机信号收到, 通知并等待子线程退出. queueName: {}", queue);
            isStopping.set(true);
            // 等待子线程退出

            long startShutdownTime = System.currentTimeMillis();
            while (true) {
                if (System.currentTimeMillis() - startShutdownTime > 30000) {
                    LOGGER.info("等待时间超过30秒, 强制退出. queueName: {}", queue);
                    break;
                }
                boolean isAllStopped = true;
                for (Thread thread : threadList) {
                    if (thread.isAlive()) {
                        isAllStopped = false;
                    }
                }
                if (isAllStopped) {
                    LOGGER.info("子线程已全部退出, 优雅停机完毕. queueName: {}", queue);
                    break;
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (InterruptedException ex2) {
                    break;
                }
            }

            //停机结束，需要关闭connection；
            //try {
            //    connectionFactory.l();
            //} catch (IOException e) {
            //    e.printStackTrace();
            //}
        }));
    }

    private class ConsumeMassageHandle<T> implements Runnable {
        private String queueName;
        private AtomicBoolean isStopping;
        private Class<T> clazz;
        private Consumer<T> msgHandler;
        private String consumerTag;
        private Map<String, Object> arguments;

        public ConsumeMassageHandle(String queueName, AtomicBoolean isStopping, String tagTail,
                                    Map<String, Object> arguments, Class<T> clazz,
                                    Consumer<T> msgHandler) {
            this.queueName = queueName;
            this.isStopping = isStopping;
            this.arguments = arguments;
            this.clazz = clazz;
            this.msgHandler = msgHandler;
            this.consumerTag = queueName + "-" + tagTail;
        }


        @Override
        public void run() {
            Channel channel = null;
            try {
                channel = getChannel();
                channel.basicConsume(queueName, false, consumerTag, false, false, arguments,
                    new DefaultConsumer(channel) {
                        @Override
                        public void handleDelivery(String consumerTag, Envelope envelope,
                                                   AMQP.BasicProperties properties, byte[] body) throws IOException {
                            if (isStopping.get()) {
                                LOGGER.info("停机信号收到, 退出消息消费处理. queueName: {}", queueName);
                                return;
                            }
                            try {
                                String message = new String(body, "UTF-8");
                                if (clazz != String.class) {
                                    T t = JSONObject.parseObject(message, clazz);
                                    msgHandler.accept(t);
                                } else {
                                    msgHandler.accept((T)message);
                                }
                                getChannel().basicAck(envelope.getDeliveryTag(), false);
                            } catch (Exception ex) {
                                // 处理失败，消息重新入队，等待下一次重试
                                getChannel().basicNack(envelope.getDeliveryTag(), false, true);

                            }
                        }
                    });
            } catch (Exception ex) {
                LOGGER.error(String.format("监听消息出现错误. 10秒钟后重新连接. queueName: %s", queueName), ex);
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException ignore) {
                }
            }
            LOGGER.info("消息监听任务已开启. queueName: {}", queueName);
            while (true) {
                try {
                    // 停机
                    if (isStopping.get()) {
                        LOGGER.info("停机信号收到, 退出循环 (一级循环). queueName: {}", queueName);
                        if (channel != null) {
                            channel.close();
                        }
                        break;
                    }
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (Exception ignore) {
                }
            }
        }
    }
}
