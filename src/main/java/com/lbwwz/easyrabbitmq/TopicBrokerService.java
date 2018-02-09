package com.lbwwz.easyrabbitmq;

/**
 * @author lbwwz
 */
public interface TopicBrokerService {
    /**
     * 发送topic消息
     */
    void publish(String topicName);

    /**
     * 监听广播消息
     */
    void subscribe();
}
