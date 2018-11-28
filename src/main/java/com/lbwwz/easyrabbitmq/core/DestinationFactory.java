package com.lbwwz.easyrabbitmq.core;

import java.io.Serializable;
import java.util.Map;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.BuiltinExchangeType;

/**
 * @author lbwwz
 */
public class DestinationFactory {
    public static Exchange buildExchange(String exchangeName, Map<String, Object> args) {
        return new TopicExchange(exchangeName, true, false, args);
    }

    public static final class ExchangeBuilder {
        private String name;
        private String type;
        private String routingKey;
        private boolean durable;
        private boolean autoDelete;
        private Map<String, Object> arguments;
        private volatile boolean delayed;
        private boolean internal;

        public ExchangeBuilder() {
            //默认持久化创建
            this.durable = true;
        }

        public ExchangeBuilder name(String name) {
            this.name = name;
            return this;
        }

        public ExchangeBuilder type(String type) {
            this.type = type;
            return this;
        }

        public ExchangeBuilder routingKey(String routingKey) {
            this.routingKey = routingKey;
            return this;
        }

        public ExchangeBuilder durable(boolean durable) {
            this.durable = durable;
            return this;
        }

        public ExchangeBuilder autoDelete(boolean autoDelete) {
            this.autoDelete = autoDelete;
            return this;
        }

        public ExchangeBuilder arguments(Map<String, Object> arguments) {
            this.arguments = arguments;
            return this;
        }

        public ExchangeBuilder delayed(boolean delayed) {
            this.delayed = delayed;
            return this;
        }

        public ExchangeBuilder internal(boolean internal) {
            this.internal = internal;
            return this;
        }

        public Exchange build() {
            AbstractExchange exchange;
            if (BuiltinExchangeType.DIRECT.name().equalsIgnoreCase(this.type)) {
                exchange = new DirectExchange(name, durable, autoDelete, arguments);
            } else if (BuiltinExchangeType.FANOUT.name().equalsIgnoreCase(this.type)) {
                exchange = new FanoutExchange(name, durable, autoDelete, arguments);
            } else {
                exchange = new TopicExchange(name, durable, autoDelete, arguments) {};
            }
            exchange.setDelayed(this.delayed);
            exchange.setDelayed(this.internal);
            return exchange;
        }

    }
}
