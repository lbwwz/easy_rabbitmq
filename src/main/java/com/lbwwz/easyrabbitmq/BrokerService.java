package com.lbwwz.easyrabbitmq;

import com.lbwwz.easyrabbitmq.core.Binding;
import com.lbwwz.easyrabbitmq.core.Exchange;
import com.lbwwz.easyrabbitmq.core.Queue;
import com.rabbitmq.client.BuiltinExchangeType;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author lbwwz
 */
public interface BrokerService {

    void exchangeDeclare(Exchange exchange);

    void queueDeclare(Queue queue);

    void queueBind(Binding binding);

    /**
     * 发送消息
     * @param messageTitle
     * @param tag
     * @param msg
     * @param <T>
     */
    <T> void publish(String messageTitle,String tag,T msg) throws IOException, TimeoutException;

    /**
     * 消费消息
     * @param messageTitle
     * @param tag
     * @param subscriptionName
     * @param threadCount
     * @param clazz
     * @param msgHandler
     * @param <T>
     */
    <T> void consume( String messageTitle,
                    String tag,
                    String subscriptionName,
                    int threadCount,
                    Class<T> clazz,
                    Consumer<T> msgHandler);
}
