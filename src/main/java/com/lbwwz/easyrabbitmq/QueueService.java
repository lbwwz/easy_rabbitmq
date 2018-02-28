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
    void listen(
            String queueName,
            int threadCount,
//            boolean enableDeadletter,
            Consumer<String> msgHandler
    );


}
