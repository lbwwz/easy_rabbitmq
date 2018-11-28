package com.lbwwz.easyrabbitmq;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import com.lbwwz.easyrabbitmq.core.Exchange;
import com.rabbitmq.client.AMQP.BasicProperties;

/**
 * @author lbwwz
 */
public interface BrokerMessageProcess {

    /**
     * 一个总概的，推送消息的方法
     *
     * @param exchangeName 要发送的exchange名称
     * @param exchangeType exchange类型
     * @param routingKey   路由键
     * @param delayTime    延时时间
     * @param msg          消息实体
     * @param <T>          泛型类型
     * @throws IOException
     * @throws TimeoutException
     */
    <T> void publish(String exchangeName, String exchangeType, String routingKey, long delayTime, T msg);



}
