package com.lbwwz.easyrabbitmq;

import com.lbwwz.easyrabbitmq.core.AbstractBroker;
import com.lbwwz.easyrabbitmq.core.SimpleRabbitAdmin;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;

/**
 * 一个topic broker包含一个主题和多个队列的映射关系
 * @author lbwwz
 */
public class TopicBroker extends AbstractBroker {

    public TopicBroker(String topicName, ConnectionFactory connectionFactory, SimpleRabbitAdmin admin) {
        super(connectionFactory,admin);
        this.setExchange(new TopicExchange(topicName,true,false));
    }

    @Override
    public void createExchange(String exchangeName) {

    }

    @Override
    public void registerQueue(){

    }
}
