package com.lbwwz.easyrabbitmq.core;


import org.springframework.amqp.core.Queue;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 一个 Exchange 经由 routeKey 与相关queue 组成的路有关系模型
 *
 * @author lbwwz
 */
public interface Broker {

    /**
     *
     * @param tag
     * @param listenerName
     * @return
     */
    Queue getRegisteredQueue(String tag,String listenerName);


    /**
     * 创建Broker
     */
    void createExchange(String name);


    /**
     * 向broker注册队列
     */
    void registerQueue();

    /**
     * 发送消息
     * @param routingKey
     * @param msg
     * @param <T>
     */
    <T> void sendMessage(String routingKey, T msg);

    /**
     * 发送延时消息
     * @param routingKey
     * @param msg
     * @param delay
     * @param timeUnit
     * @param <T>
     */
    <T> void sendDelayMessage(String routingKey, T msg, int delay, TimeUnit timeUnit);

}
