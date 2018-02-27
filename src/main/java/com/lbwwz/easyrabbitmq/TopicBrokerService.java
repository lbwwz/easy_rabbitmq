package com.lbwwz.easyrabbitmq;

import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author lbwwz
 */
public interface TopicBrokerService{
    /**
     * 发送topic消息
     */
    <T> void publish(String topicName,String tag,T msg);

    /**
     * 监听广播消息
     */
    <T> void subscribe( String topicName,
                    String tag,
                    String subscriptionName,
                    int threadCount,
                    Class<T> clazz,
                    Consumer<T> msgHandler);
}
