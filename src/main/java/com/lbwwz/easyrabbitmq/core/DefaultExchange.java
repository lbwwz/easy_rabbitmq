package com.lbwwz.easyrabbitmq.core;

import java.util.Map;

import com.rabbitmq.client.BuiltinExchangeType;

/**
 * @author lbwwz
 */
public class DefaultExchange extends AbstractExchange {

    public DefaultExchange(String name) {
        super(name);
    }

    public DefaultExchange(String name, boolean durable, boolean autoDelete) {
        super(name, durable, autoDelete);
    }

    public DefaultExchange(String name, boolean durable, boolean autoDelete,
                           Map<String, Object> arguments) {
        super(name, durable, autoDelete, arguments);
    }

    @Override
    public String getType() {
        return BuiltinExchangeType.TOPIC.getType();
    }
}
