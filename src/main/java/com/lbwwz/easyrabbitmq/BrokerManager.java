package com.lbwwz.easyrabbitmq;

import java.io.IOException;

import com.lbwwz.easyrabbitmq.core.Binding;
import com.lbwwz.easyrabbitmq.core.Exchange;
import com.lbwwz.easyrabbitmq.core.Queue;
import com.rabbitmq.client.Channel;

/**
 * @author lbwwz
 */
public interface BrokerManager {

    /**
     * 定义相应的交换器
     * @param channel
     * @param exchange
     * @throws IOException
     */
    void declareExchange(final Channel channel, final Exchange exchange) throws IOException;

    /**
     * 定义队列
     * @param channel
     * @param queue
     * @throws IOException
     */
    void declareQueue(final Channel channel, final Queue queue) throws IOException;

    /**
     * 定义交换器和队列的绑定关系
     * @param channel
     * @param binding
     * @throws IOException
     */
    void declareBinding(final Channel channel, final Binding binding) throws IOException;
}
