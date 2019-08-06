package com.lbwwz.easyrabbitmq.core;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

/**
 * @author lbwwz
 */
public class Queue {

    private final String name;

    private final boolean durable;

    private final boolean exclusive;

    private final boolean autoDelete;

    private final java.util.Map<java.lang.String, java.lang.Object> arguments;

    public Queue(String name) {
        this(name, true, false, false);
    }

    public Queue(String name, boolean durable) {
        this(name, durable, false, false, null);
    }

    public Queue(String name, boolean durable, boolean exclusive, boolean autoDelete) {
        this(name, durable, exclusive, autoDelete, null);
    }

    public Queue(String name, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments) {
        Assert.notNull(name, "'name' cannot be null");
        this.name = name;
        this.durable = durable;
        this.exclusive = exclusive;
        this.autoDelete = autoDelete;
        this.arguments = arguments;
    }

    public String getName() {
        return this.name;
    }

    public boolean isDurable() {
        return this.durable;
    }

    public boolean isExclusive() {
        return this.exclusive;
    }

    public boolean isAutoDelete() {
        return this.autoDelete;
    }

    public java.util.Map<java.lang.String, java.lang.Object> getArguments() {
        return this.arguments;
    }

    @Override
    public String toString() {
        return "Queue [name=" + this.name + ", durable=" + this.durable + ", autoDelete=" + this.autoDelete
            + ", exclusive=" + this.exclusive + ", arguments=" + this.arguments + "]";
    }



}
