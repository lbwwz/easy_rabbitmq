package com.lbwwz.easyrabbitmq;

import java.util.function.Consumer;

/**
 * @author lbwwz
 */
public interface QueueService {

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

    <T> void listen(
            String queueName,
            int threadCount,
            Consumer<String> msgHandler
    );


}
