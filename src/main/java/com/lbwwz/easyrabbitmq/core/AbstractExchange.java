package com.lbwwz.easyrabbitmq.core;

import java.util.HashMap;
import java.util.Map;

import com.rabbitmq.client.BuiltinExchangeType;

/**
 * @author lbwwz
 */


public abstract class AbstractExchange implements Exchange {

    private final String name;

    private final boolean durable;

    private final boolean autoDelete;

    private final Map<String, Object> arguments;

    private volatile boolean delayed;

    private boolean internal;


    public AbstractExchange(String name) {
        this(name, true, false);
    }


    public AbstractExchange(String name, boolean durable, boolean autoDelete) {
        this(name, durable, autoDelete, null);
    }


    public AbstractExchange(String name, boolean durable, boolean autoDelete, Map<String, Object> arguments) {
        super();
        this.name = name;
        this.durable = durable;
        this.autoDelete = autoDelete;
        if (arguments != null) {
            this.arguments = arguments;
        }
        else {
            this.arguments = new HashMap<>();
        }
    }

    @Override
    public abstract String getType();

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean isDurable() {
        return this.durable;
    }

    @Override
    public boolean isAutoDelete() {
        return this.autoDelete;
    }


    protected synchronized void addArgument(String argName, Object argValue) {
        this.arguments.put(argName, argValue);
    }

    @Override
    public Map<String, Object> getArguments() {
        return this.arguments;
    }

    @Override
    public boolean isDelayed() {
        return this.delayed;
    }

    public void setDelayed(boolean delayed) {
        this.delayed = delayed;
    }

    @Override
    public boolean isInternal() {
        return this.internal;
    }

    public void setInternal(boolean internal) {
        this.internal = internal;
    }

    @Override
    public String toString() {
        return "Exchange [name=" + this.name +
            ", type=" + getType() +
            ", durable=" + this.durable +
            ", autoDelete=" + this.autoDelete +
            ", internal=" + this.internal +
            ", arguments="	+ this.arguments + "]";
    }

}

