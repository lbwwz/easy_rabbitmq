package com.lbwwz.easyrabbitmq.cache;

import com.lbwwz.easyrabbitmq.QueueServiceImpl;
import com.lbwwz.easyrabbitmq.exception.MqConnectionFailedException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmListener;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * rabbmq 连接池，对Connection和channel做一定的功能封装和增强
 * 待改造，需要支持连接池技术
 *
 * @author lbwwz.
 */
public class MqCacheConnectionFactory {

    private String vHost;
    private String host;
    private String userName;
    private String password;

    public MqCacheConnectionFactory() {

        this.host = "localhost";
        this.userName = "lbwwz";
        this.password = "123456";
        this.vHost = "/test";
    }

    /**
     * 标记当前连接池与mq服务连接是否中断
     */
    private volatile AtomicBoolean isInterruptIed = new AtomicBoolean(false);

    //ShutdownListener

    private static final Logger LOGGER = LoggerFactory.getLogger(MqCacheConnectionFactory.class);
    /**
     * 一个连接所包含的最大channel数量
     */
    private final static int CONNECTION_CHANNEL_MAX_SIZE = 10;

    //非事务的连接
    private final LinkedBlockingQueue<Connection> generallyConnectionQueue = new LinkedBlockingQueue<>();
    //启用事务的连接
    private final LinkedBlockingQueue<Connection> transactionalConnectionQueue = new LinkedBlockingQueue<>();

    /**
     * 作为守护线程，负责维护指定的connection中的channel
     */
    ExecutorService executorService = Executors.newCachedThreadPool();

    private class ChannelCachingConnectionProxy {
        private boolean isTransactional;
        private volatile Connection target;
        private Semaphore semaphore = new Semaphore(CONNECTION_CHANNEL_MAX_SIZE);
        private List<ChannelProxy> channels;

        public Connection getTarget() {
            return target;
        }

        private ChannelProxy getCacheChannel() {
            ChannelProxy channelProxy = null;
            try {
                semaphore.acquire();
                channelProxy = (ChannelProxy)Proxy.newProxyInstance(ChannelProxy.class.getClassLoader(),
                    new Class[] {ChannelProxy.class},
                    new ChannelProxyInvocationHandler(ChannelCachingConnectionProxy.this.target,
                        ChannelCachingConnectionProxy.this.isTransactional));

            } catch (InterruptedException e2) {
                //中断异常，表示信号量使用完毕，需要重新创建一个连接

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("当前连接所创建的 channel 已达到上限");
                }
            }
            return channelProxy;
        }
    }

    /**
     * Channel 增强类
     */
    private class ChannelProxyInvocationHandler implements InvocationHandler {

        private Connection targetConnection;
        private Channel targetChannel;
        /**
         * 标记创建的渠道是否开启事务
         */
        private boolean isTransactional;

        public ChannelProxyInvocationHandler(Connection targetConnection, boolean isTransactional) {
            this.targetConnection = targetConnection;
            try {
                this.targetChannel = getBareChannel();
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.isTransactional = isTransactional;
            if (this.isTransactional) {
                //开启事务
                this.targetChannel.addConfirmListener(new ConfirmListener() {
                    @Override
                    public void handleAck(long deliveryTag, boolean multiple) throws IOException {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.info("Message deliveryTag :{} 发送成功！", deliveryTag);
                        }
                    }

                    @Override
                    public void handleNack(long deliveryTag, boolean multiple) throws IOException {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.info("Message deliveryTag :{} 发送失败！", deliveryTag);
                        }
                        //当前消息发送失败
                    }
                });
            }
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            //关于渠道是否支持事务的拓展
            if (method.getName().equals("isTransactional")) {
                return ChannelProxyInvocationHandler.this.isTransactional;
            }
            return method.invoke(targetChannel, args);
        }

        private Channel getBareChannel() throws IOException {
            Channel channel = ChannelProxyInvocationHandler.this.targetConnection.createChannel();
            return channel;

        }




        public Connection getConnection() throws IOException, TimeoutException {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(host);
            factory.setUsername(userName);
            factory.setPassword(password);
            factory.setVirtualHost(vHost);
            return factory.newConnection();
        }
    }
}
