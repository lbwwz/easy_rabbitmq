package com.lbwwz.easyrabbitmq;

import java.util.function.Consumer;

/**
 * 队列服务
 * @author lbwwz
 */
public interface QueueService {

    <T> void listen(String queueName, Consumer<String> msgHandler);

    <T> void listen(
        String queueName,
        int threadCount,
        Consumer<String> msgHandler
    );

    /**
     * 队列监听
     * @param queueName
     * @param threadCount
     * @param msgHandler
     */
    <T> void listen(
            String queueName,
            int threadCount,
            Consumer<T> msgHandler,
            Class<T> clazz
    );
}
