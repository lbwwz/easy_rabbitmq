package com.lbwwz.easyrabbitmq;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import com.alibaba.fastjson.JSONObject;

import com.lbwwz.easyrabbitmq.core.Binding;
import com.lbwwz.easyrabbitmq.core.Exchange;
import com.lbwwz.easyrabbitmq.core.Queue;
import com.lbwwz.easyrabbitmq.core.SimpleRabbitAdmin;
import com.lbwwz.easyrabbitmq.util.MqNameUtil;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author lbwwz
 */
public class AbstractBrokerServiceImpl implements BrokerService, ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueueServiceImpl.class);

    private SimpleRabbitAdmin simpleRabbitAdmin;

    private ConnectionFactory connectionFactory;

    private final static int DEFAULT_LISTEN_THREAD_COUNT =5;


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        //这里做相关的connection获取配置
        this.connectionFactory = applicationContext.getBean(ConnectionFactory.class);
        this.simpleRabbitAdmin = new SimpleRabbitAdmin(this.connectionFactory);
    }

    @Override
    public void exchangeDeclare(Exchange exchange) {

    }

    @Override
    public void queueDeclare(Queue queue) {

    }
    @Override
    public void queueBind(Binding binding) {
    }

    private Channel getChannel() throws IOException, TimeoutException {
            Connection connection = connectionFactory.newConnection();
            return connection.createChannel();

    }
    //尝试创建之后将已经创建成功的exchange记录在这里



    @Override
    public <T> void publish(String topicName, String tag, T msg) throws IOException, TimeoutException {
        //先尝试创建 exchange
        //simpleRabbitAdmin.declareExchange(getChannel(),);
        //发送相关的消息


    }

    /**
     * 监听消息
     * <p>
     *     消息监听方设置相关的监听队列和 tag
     * </p>
     *
     * @param messageTitle        消息名称，用来确定监听的消息对象
     * @param tag              路由键，用于选择性的接收消息
     * @param subscriptionName 监听者的名称，用来映射是生成队列名称
     * @param threadCount      同时监听的线程数
     * @param clazz            消息实体的类型
     * @param msgHandler       消息处理方法
     * @param <T>              消息类型
     */
    @Override
    public <T> void consume(String messageTitle, String tag, String subscriptionName, int threadCount,
                              Class<T> clazz, Consumer<T> msgHandler) {

        String queueName = MqNameUtil.makeQueueName(messageTitle);
        // 定义停机信号
        AtomicBoolean isStopping = new AtomicBoolean();

        Runnable listenAction = () -> {
            Channel channel = null;
            try {
                channel = getChannel();
                channel.basicConsume(queueName, true, new DefaultConsumer(channel) {
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
                        } catch (Exception ex) {
                            //todo 处理失败，这里可以做一些补偿处理
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

            long startShutdownTime = System.currentTimeMillis();
            while (true) {
                if(System.currentTimeMillis()-startShutdownTime>30000){
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

}
