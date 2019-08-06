package com.lbwwz.easyrabbitmq.cache;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 实现一个channel的缓存池
 *
 * @author lbwwz.
 */
public class MqConnectionFactory {

    private final String vHost;
    private String hostName;
    private String userName;
    private String password;
    private ConnectionFactory factory;

    private static final Logger LOGGER = LoggerFactory.getLogger(MqConnectionFactory.class);

    //    CachingConnectionFactory

    private static class LazyHolder {
        private static final MqConnectionFactory INSTANCE = new MqConnectionFactory();
    }

    private MqConnectionFactory() {
        this.hostName = "localhost";
        this.userName = "lbwwz";
        this.password = "123456";
        this.vHost = "/test";

        factory = new ConnectionFactory();
        factory.setHost(hostName);
        factory.setUsername(userName);
        factory.setPassword(password);
        factory.setVirtualHost(vHost);
    }

    protected final String getDefaultHostName() {
        String temp;
        try {
            InetAddress localMachine = InetAddress.getLocalHost();
            temp = localMachine.getHostName();
            LOGGER.debug("Using hostname [" + temp + "] for hostname.");
        } catch (UnknownHostException e) {
            LOGGER.warn("Could not get host name, using 'localhost' as default value", e);
            temp = "localhost";
        }
        return temp;
    }

    public static MqConnectionFactory getInstance() {
        return LazyHolder.INSTANCE;
    }

    public Connection getConnection() throws IOException, TimeoutException {
        return factory.newConnection();
    }

}
