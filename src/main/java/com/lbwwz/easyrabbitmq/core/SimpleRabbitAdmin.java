package com.lbwwz.easyrabbitmq.core;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.Queue.DeclareOk;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ChannelProxy;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * 添加运行时对 Exchange 和Queue 的定义和绑定功能
 *
 * @author lbwwz
 */
public class SimpleRabbitAdmin extends RabbitAdmin {

    private static final String DELAYED_MESSAGE_EXCHANGE = "x-delayed-message";
    private static final String AMQP_PRE = "amq.";

    public SimpleRabbitAdmin(ConnectionFactory connectionFactory) {
        super(connectionFactory);
    }

    public void declareExchanges(final Channel channel, final Exchange exchange) throws IOException {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("declaring Exchange '" + exchange.getName() + "'");
        }
        if (!DEFAULT_EXCHANGE_NAME.equals(exchange.getName())) {
            try {
                if (exchange.isDelayed()) {
                    Map<String, Object> arguments = exchange.getArguments();
                    if (arguments == null) {
                        arguments = new HashMap<>();
                    } else {
                        arguments = new HashMap<>(arguments);
                    }
                    arguments.put("x-delayed-type", exchange.getType());
                    channel.exchangeDeclare(exchange.getName(), DELAYED_MESSAGE_EXCHANGE, exchange.isDurable(),
                            exchange.isAutoDelete(), exchange.isInternal(), arguments);
                } else {
                    channel.exchangeDeclare(exchange.getName(), exchange.getType(), exchange.isDurable(),
                            exchange.isAutoDelete(), exchange.isInternal(), exchange.getArguments());
                }
            } catch (IOException ignore) {
                //重复定义持久化Exchange
            }
        }
    }

    public void declareQueues(final Channel channel, final Queue queue) throws IOException {
        if (!queue.getName().startsWith(AMQP_PRE)) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("declaring Queue '" + queue.getName() + "'");
            }
            try {
                try {
                    channel.queueDeclare(queue.getName(), queue.isDurable(), queue.isExclusive(), queue.isAutoDelete(),
                            queue.getArguments());
                }
                catch (IllegalArgumentException e) {
                    if (this.logger.isDebugEnabled()) {
                        this.logger.error("Exception while declaring queue: '" + queue.getName() + "'");
                    }
                    try {
                        if (channel instanceof ChannelProxy) {
                            ((ChannelProxy) channel).getTargetChannel().close();
                        }
                    }
                    catch (TimeoutException ignore) {
                    }
                    throw new IOException(e);
                }
            }
            catch (IOException ignore) {}
        } else if (this.logger.isDebugEnabled()) {
            this.logger.debug("Queue with name that starts with 'amq.' cannot be declared.");
        }
    }

    public void declareBindings(final Channel channel, final Binding... bindings) throws IOException {
        for (Binding binding : bindings) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Binding destination [" + binding.getDestination() + " (" + binding.getDestinationType()
                        + ")] to exchange [" + binding.getExchange() + "] with routing key [" + binding.getRoutingKey()
                        + "]");
            }

            try {
                if (binding.isDestinationQueue()) {
                    if (!DEFAULT_EXCHANGE_NAME.equals((binding.getExchange())) && binding.getDestination()
                                    .equals(binding.getRoutingKey())) {
                        channel.queueBind(binding.getDestination(), binding.getExchange(), binding.getRoutingKey(),
                                binding.getArguments());
                    }
                } else {
                    channel.exchangeBind(binding.getDestination(), binding.getExchange(), binding.getRoutingKey(),
                            binding.getArguments());
                }
            } catch (IOException ignore) {
                //绑定的对象不存在或者绑定失败
            }
        }
    }

}
