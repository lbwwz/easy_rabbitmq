package com.lbwwz.easyrabbitmq.core;


/**
 * 一个 Exchange 经由 routeKey 与相关queue 组成的路有关系模型
 *
 * @author lbwwz
 */
public interface Broker {


    /**
     * 创建Broker
     */
    void createExchange(String name);


    /**
     * 向broker注册队列
     */
    void registerQueue();


}
