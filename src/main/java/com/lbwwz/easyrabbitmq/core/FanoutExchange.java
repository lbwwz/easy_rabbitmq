package com.lbwwz.easyrabbitmq.core;

import java.util.Map;

import com.rabbitmq.client.BuiltinExchangeType;

/**
 * @author lbwwz
 */
public class FanoutExchange extends AbstractExchange {

    public FanoutExchange(String name) {
        super(name);
    }

    public FanoutExchange(String name, boolean durable, boolean autoDelete) {
        super(name, durable, autoDelete);
    }

    public FanoutExchange(String name, boolean durable, boolean autoDelete,
                          Map<String, Object> arguments) {
        super(name, durable, autoDelete, arguments);
    }

    @Override
    public String getType() {
        return BuiltinExchangeType.FANOUT.getType();
    }
}
