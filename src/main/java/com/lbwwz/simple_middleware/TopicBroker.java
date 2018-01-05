package com.lbwwz.simple_middleware;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;

/**
 * 一个topic broker包含一个主题和多个队列的映射关系
 * @author lbwwz
 */
public class TopicBroker extends AbstractBroker{

    protected TopicBroker(ConnectionFactory connectionFactory) {
        super(connectionFactory);
    }





}
