package com.lbwwz.easyrabbitmq.core;

import java.util.Map;

import com.rabbitmq.client.BuiltinExchangeType;

/**
 * @author lbwwz
 */
public class TopicExchange extends AbstractExchange {

    public TopicExchange(String name) {
        super(name);
    }

    /**
     *
     * @param name 交换器名称
     * @param durable 是否持久化
     * @param autoDelete
     */
    public TopicExchange(String name, boolean durable, boolean autoDelete) {
        super(name, durable, autoDelete);
    }

    public TopicExchange(String name, boolean durable, boolean autoDelete,
                         Map<String, Object> arguments) {
        super(name, durable, autoDelete, arguments);
    }

    @Override
    public String getType() {
        return BuiltinExchangeType.TOPIC.getType();
    }
}
