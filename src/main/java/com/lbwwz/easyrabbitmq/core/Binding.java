package com.lbwwz.easyrabbitmq.core;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author lbwwz
 */
public class Binding {

    private final String queue;

    private final String exchange;

    private final List<String> routingKeys;

    private final Map<String, Object> arguments;

    public Binding(String queue, String exchange, Map<String, Object> arguments, String... routingKeys) {
        this.queue = queue;
        this.exchange = exchange;
        this.arguments = arguments;
        this.routingKeys = Arrays.asList(routingKeys);
    }

    public String getQueue() {
        return this.queue;
    }

    public String getExchange() {
        return this.exchange;
    }

    public List<String> getRoutingKeys() {
        return routingKeys;
    }

    public Map<String, Object> getArguments() {
        return this.arguments;
    }

    @Override
    public String toString() {
        return "Binding{" +
            "queue='" + queue + '\'' +
            ", exchange='" + exchange + '\'' +
            ", routingKeys=" + routingKeys +
            ", arguments=" + arguments +
            '}';
    }
}
