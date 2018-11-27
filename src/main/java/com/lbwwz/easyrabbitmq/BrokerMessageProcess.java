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

    /**
     * * 一个总概的，对队列消息消费的方法
     *
     * @param queue       队列名称
     * @param autoAck     是否自动确认消息,true自动确认,false 不自动要手动调用,建立设置为false
     * @param consumerTag 消费者标签，用来区分多个消费者
     * @param arguments   消费者的参数
     * @param threadCount 消费当前消息的线程数
     * @param clazz       消息实体类型
     * @param msgHandler  消息的处理方法
     * @param <T>         泛型
     */
    <T> void consume(String queue,
                     boolean autoAck,
                     String consumerTag,
                     Map<String, Object> arguments,
                     int threadCount,
                     Class<T> clazz,
                     Consumer<T> msgHandler);

}
