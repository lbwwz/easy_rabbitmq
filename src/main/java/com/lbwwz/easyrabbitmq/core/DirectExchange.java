package com.lbwwz.easyrabbitmq.core;

import java.util.Map;

import com.rabbitmq.client.BuiltinExchangeType;

/**
 * @author lbwwz
 */
public class DirectExchange extends AbstractExchange {

    public DirectExchange(String name) {
        super(name);
    }

    public DirectExchange(String name, boolean durable, boolean autoDelete) {
        super(name, durable, autoDelete);
    }

    public DirectExchange(String name, boolean durable, boolean autoDelete,
                          Map<String, Object> arguments) {
        super(name, durable, autoDelete, arguments);
    }

    @Override
    public String getType() {
        return BuiltinExchangeType.DIRECT.getType();
    }
}
