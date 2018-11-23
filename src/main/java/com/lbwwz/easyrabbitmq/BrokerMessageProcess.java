package com.lbwwz.easyrabbitmq;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import com.rabbitmq.client.AMQP.BasicProperties;

/**
 * @author lbwwz
 */
public interface BrokerMessageProcess {

    /**
     * 一个总概的，推送消息的方法
     *
     * @param exchange   要发送的exchange
     * @param routingKey 路由键
     * @param mandatory  当没有任何一个消费队列时候，是否通过return方法将消息返回
     * @param props      相关配置参数
     * @param msg        消息实体，发送的消息将 json格式化
     * @param <T>
     * @throws IOException
     * @throws TimeoutException
     */
    <T> void publish(String exchange, String routingKey, boolean mandatory, BasicProperties props, T msg)
        throws IOException, TimeoutException;

    /**
     * 一个总概的，对队列消息消费的方法
     * @param queue
     * @param autoAck 是否自动确认消息,true自动确认,false 不自动要手动调用,建立设置为false
     * @param consumerTag 消费者标签，用来区分多个消费者
     * @param arguments 消费者的参数
     * @param clazz
     * @param msgHandler
     * @param <T>
     */
    <T> void consume(String queue,
                     boolean autoAck,
                     String consumerTag,
                     Map<String, Object> arguments,
                     int ThreadCount,
                     Class<T> clazz,
                     Consumer<T> msgHandler);

    //String queue, boolean autoAck, String consumerTag, boolean noLocal, boolean exclusive, Map<String, Object>
    // arguments, com.rabbitmq.client.Consumer
    //
    //callback) throws IOException;

}
