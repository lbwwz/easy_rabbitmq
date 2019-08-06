package com.lbwwz.easyrabbitmq.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.common.collect.Lists;
import com.lbwwz.easyrabbitmq.util.Constant;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.ArrayUtils;

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

        List<String> routingKeyList = Lists.newArrayList(routingKeys);

        routingKeyList.removeIf(Objects::isNull);

        if(CollectionUtils.isEmpty(routingKeyList)){
            this.routingKeys = Collections.singletonList(Constant.DEFAULT_ROUTING_KEY);
        }else{
            this.routingKeys = routingKeyList;
        }

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
