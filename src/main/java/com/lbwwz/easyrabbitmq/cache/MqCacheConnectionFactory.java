package com.lbwwz.easyrabbitmq.cache;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

import com.lbwwz.easyrabbitmq.exception.MqConnectionFailedException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmListener;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(host);
        connectionFactory.setUsername(userName);
        connectionFactory.setPassword(password);
        connectionFactory.setVirtualHost(vHost);
    }

    private final ConnectionFactory connectionFactory;

    /**
     * 标记当前连接池与mq服务连接是否中断 todo 考虑做动态重连
     */
    private volatile AtomicBoolean isInterrupted = new AtomicBoolean(false);
    //使用ShutdownListener

    private static final Logger LOGGER = LoggerFactory.getLogger(MqCacheConnectionFactory.class);
    /**
     * 一个连接所包含的最大channel数量
     */
    private static final int CONNECTION_CHANNEL_MAX_SIZE = 10;

    /**
     * 非事务支持的的连接
     */
    private final LinkedBlockingQueue<ChannelCachingConnectionProxy> generallyConnectionQueue
        = new LinkedBlockingQueue<>();
    /**
     * 启用事务支持的连接
     */
    private final LinkedBlockingQueue<ChannelCachingConnectionProxy> transactionalConnectionQueue
        = new LinkedBlockingQueue<>();

    //
    private final HashMap<ChannelCachingConnectionProxy, List<ChannelProxy>>
        channelCachingConnectionWithChannelListMapper = new HashMap<>();

    /**
     * 作为守护线程，负责维护指定的connection中的channel todo 设计为动态配置
     */
    ExecutorService executorService = Executors.newCachedThreadPool();

    public ChannelProxy generateConnectionCacheChannel(boolean isTransactional) {
        ChannelProxy channelProxy = null;
        for (int i = 0; i < 5; i++) {
            channelProxy = getChannelCachingConnectionProxy(isTransactional).getCacheChannel();
            if (channelProxy != null) {
                break;
            }
        }
        if (channelProxy != null) {
            // todo 走到这里说明代码中需要优化
            LOGGER.error("generateConnectionCacheChannel 走到这里说明代码中需要优化");
        }
        return channelProxy;
    }

    /**
     * @param isTransactional 是否支持事务
     * @return {@link ChannelCachingConnectionProxy}
     */
    private synchronized ChannelCachingConnectionProxy getChannelCachingConnectionProxy(boolean isTransactional) {
        LinkedBlockingQueue<ChannelCachingConnectionProxy> activeConnectionQueue = pickActiveCachingConnectionQueue(
            isTransactional);
        ChannelCachingConnectionProxy channelCachingConnectionProxy = activeConnectionQueue.peek();
        if (channelCachingConnectionProxy == null) {
            for (int i = 0; ; i++) {
                try {
                    channelCachingConnectionProxy = new ChannelCachingConnectionProxy(isTransactional,
                        connectionFactory.newConnection());
                    activeConnectionQueue.put(channelCachingConnectionProxy);
                    break;
                } catch (Exception e) {
                    if (i >= 3) {
                        throw new MqConnectionFailedException("创建 connection 连接失败", e);
                    }
                }
            }
        }
        return channelCachingConnectionProxy;
    }

    /**
     * 根据是否支持事务选择
     *
     * @param isTransactional 是否支持事务
     */
    private LinkedBlockingQueue<ChannelCachingConnectionProxy> pickActiveCachingConnectionQueue(
        boolean isTransactional) {
        return isTransactional ? this.transactionalConnectionQueue : this.generallyConnectionQueue;
    }

    /**
     * Connection 连接的代理类
     */
    private class ChannelCachingConnectionProxy {
        /**
         * 连接是否支出事务
         */
        private volatile boolean isTransactional;
        private volatile boolean isOpen;
        private volatile AtomicBoolean isFull = new AtomicBoolean(false);
        private volatile Connection target;
        private Semaphore semaphore = new Semaphore(CONNECTION_CHANNEL_MAX_SIZE);
        private List<ChannelProxy> channels;

        ChannelCachingConnectionProxy(boolean isTransactional, Connection target) {
            this.isTransactional = isTransactional;
            this.target = target;
            this.channels = new ArrayList<>();
        }

        public Connection getTarget() {
            return target;
        }



        private ChannelProxy getCacheChannel() {
            synchronized (ChannelCachingConnectionProxy.this) {
                ChannelProxy channelProxy = null;
                try {
                    if (isFull.get()) {
                        channelProxy = generateConnectionCacheChannel(this.isTransactional);
                    }
                    semaphore.acquire();
                    channelProxy = (ChannelProxy)Proxy.newProxyInstance(ChannelProxy.class.getClassLoader(),
                        new Class[] {ChannelProxy.class},
                        new ChannelProxyInvocationHandler(ChannelCachingConnectionProxy.this.target,
                            ChannelCachingConnectionProxy.this.isTransactional));
                    channels.add(channelProxy);
                } catch (InterruptedException ex) {
                    //中断异常，表示信号量使用完毕，需要重新创建一个 ChannelCachingConnectionProxy
                    LOGGER.warn("Interrupted!", ex);
                    Thread.currentThread().interrupt();

                }
                return channelProxy;
            }
        }
        //if (LOGGER.isDebugEnabled()) {
        //    LOGGER.debug("当前连接所创建的 channel 已达到上限");
        //}
        ////当前头元素因为channel数满而出队
        //pickActiveCachingConnectionQueue(this.isTransactional).poll();
        //            isFull.set(true);

        @Override
        public boolean equals(Object o) {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }
            ChannelCachingConnectionProxy that = (ChannelCachingConnectionProxy)o;
            return this.target.equals(that.target);
        }

        @Override
        public int hashCode() {
            return target.hashCode();
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
                //事务
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
                        // todo 当前消息发送失败的处理 publish failed 事务相关
                    }
                });
            }
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            //关于渠道是否支持事务的拓展
            if ("isTransactional".equals(method.getName())) {
                return ChannelProxyInvocationHandler.this.isTransactional;
            }
            return method.invoke(targetChannel, args);
        }

        private Channel getBareChannel() throws IOException {
            Channel channel = ChannelProxyInvocationHandler.this.targetConnection.createChannel();
            // todo 相关功能支持？
            return channel;
        }
    }
}
