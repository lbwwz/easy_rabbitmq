package com.lbwwz.easyrabbitmq;

import java.util.Map;
import java.util.function.Consumer;

/**
 * 队列服务
 *
 * @author lbwwz
 */
public interface QueueService {

    /**
     * * 一个总概的，对队列消息消费的方法
     *
     * @param msgTitle    发送指定的消息标题
     * @param consumerTag 消费者标签，用来区分多个消费者
     * @param arguments   消费者的参数
     * @param threadCount 消费当前消息的线程数
     * @param clazz       消息实体类型
     * @param msgHandler  消息的处理方法
     * @param <T>         泛型
     */
    <T> void consume(String msgTitle,
                     String consumerTag,
                     Map<String, Object> arguments,
                     int threadCount,
                     Class<T> clazz,
                     Consumer<T> msgHandler);
}
