package com.lbwwz.easyrabbitmq.core;

import java.util.Map;

import com.rabbitmq.client.BuiltinExchangeType;

/**
 * @author lbwwz
 */
public interface Exchange {

    String getName();

    String getType();

    boolean isDurable();

    boolean isAutoDelete();

    Map<String, Object> getArguments();

    boolean isDelayed();

    boolean isInternal();

}
