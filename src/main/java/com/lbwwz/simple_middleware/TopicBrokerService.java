package com.lbwwz.simple_middleware;

/**
 * @author lbwwz
 */
public interface TopicBrokerService {
    /**
     * 发送topic消息
     */
    void publish();

    /**
     * 监听广播消息
     */
    void subscribe();
}
