package com.lbwwz.easyrabbitmq;

import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * @author lbwwz
 */
public class QueueServiceImpl implements QueueService{

    private static final Logger LOGGER = LoggerFactory.getLogger(QueueService.class);

    private ConnectionFactory connectionFactory;

    public QueueServiceImpl(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public void listen(String queueName, int threadCount, Consumer<String> msgHandler) {
        // 定义停机信号
        AtomicBoolean isStopping = new AtomicBoolean();
        Runnable listenAction = () -> {
            Channel channel = null;
            try {
                channel = connectionFactory.newConnection().createChannel();
                channel.basicConsume(queueName, true, new DefaultConsumer(channel) {
                    @Override
                    public void handleDelivery(String consumerTag, Envelope envelope,
                                               AMQP.BasicProperties properties, byte[] body) throws IOException {

                        if (isStopping.get()) {
                            LOGGER.info("停机信号收到, 退出消息消费处理. queueName: {}", queueName);
                            return;
                        }
                        String message = new String(body, "UTF-8");
                        System.out.println("ReceiveLogsTopic2 [x] Received '" + envelope.getRoutingKey()
                                + "':'" + message + "'");
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
        };


        // 多线程支持
        List<Thread> threadList = new ArrayList<>();
        for (int i = 0; i < threadCount; ++i) {
            Thread thread = new Thread(listenAction);
            thread.start();
            threadList.add(thread);
        }

        // 监听系统停机信号
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("停机信号收到, 通知并等待子线程退出. queueName: {}", queueName);
            isStopping.set(true);
            // 等待子线程退出
            int waitMilliseconds = 0;
            while (true) {
                if (waitMilliseconds >= 30000) {
                    LOGGER.info("等待时间超过30秒, 强制退出. queueName: {}", queueName);
                    break;
                }

                boolean isAllStopped = true;
                for (Thread thread : threadList) {
                    if (thread.isAlive()) {
                        isAllStopped = false;
                    }
                }

                if (isAllStopped) {
                    LOGGER.info("子线程已全部退出, 优雅停机完毕. queueName: {}", queueName);
                    break;
                }

                try {
                    Thread.sleep(100);
                    waitMilliseconds += 100;
                } catch (InterruptedException ex2) {
                    break;
                }
            }
        }));
    }
}
