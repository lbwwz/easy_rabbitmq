package com.lbwwz.simple_middleware;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 *
 * @author lbwwz.
 */
public class MqConnectionFactory {

    private final String vHost;
    private String host;
    private String userName;
    private String password;

//    private CacheConnection

    /**
     *
     */
    public MqConnectionFactory(){


        this.host = "localhost";
        this.userName = "lbwwz";
        this.password = "123456";
        this.vHost = "/test";
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
