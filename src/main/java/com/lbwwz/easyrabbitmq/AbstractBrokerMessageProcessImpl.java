package com.lbwwz.easyrabbitmq;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import com.alibaba.fastjson.JSONObject;

import com.lbwwz.easyrabbitmq.core.DestinationFactory.ExchangeBuilder;
import com.lbwwz.easyrabbitmq.core.Exchange;
import com.lbwwz.easyrabbitmq.util.MqBizUtil;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.AMQP.BasicProperties.Builder;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * <p>
 * 性能排序：fanout > direct >> topic。比例大约为11：10：6
 * </p>
 *
 * @author lbwwz
 */
public abstract class AbstractBrokerMessageProcessImpl extends AbstractBrokerManager
    implements BrokerMessageProcess {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueueServiceImpl.class);

    private final static int DEFAULT_LISTEN_THREAD_COUNT = 5;

    private final static String MESSAGE_CONTENT_TYPE = "application/json";

    public AbstractBrokerMessageProcessImpl() {
        super();
    }

    private Channel getChannel() throws IOException, TimeoutException {
        Connection connection = connectionFactory.newConnection();
        return connection.createChannel();

    }
    //尝试创建之后将已经创建成功的exchange记录在这里

    @Override
    public <T> void publish(String exchangeName, String exchangeType, String routingKey, long delayTime, T msg) {

        this.publish(exchangeName, exchangeType, routingKey, delayTime, msg);
    }

    /**
     * 具体的消息发送的实现方法
     *
     * @param exchangeName
     * @param exchangeType
     * @param routingKey
     * @param delayTime
     * @param msg
     * @param <T>
     */
    public <T> void publish(String exchangeName, String exchangeType, String routingKey, Long delayTime, T msg) {
        Exchange exchange = new ExchangeBuilder().name(exchangeName).type(exchangeType).delayed(delayTime != null)
            .build();
        try {
            Channel channel = getChannel();
            declareExchange(channel, exchange);
            Builder propsBuilder = new Builder().contentType(MESSAGE_CONTENT_TYPE);
            if (exchange.isDelayed()) {
                //延时消息设置延迟时间
                Map<String, Object> headers = new HashMap<>();
                headers.put("x-delay", delayTime);
                propsBuilder.headers(headers);
            }
            channel.confirmSelect();
            channel.basicPublish(exchange.getName(), routingKey, propsBuilder.build(), JSONObject.toJSONBytes(msg));
            if (channel.waitForConfirms()) {
                System.out.println("消息发送成功");
            }
        } catch (Exception e) {
            // todo 异常

        }
    }

    public <T> void publish(String exchangeName, String exchangeType, String routingKey, T msg) {
        this.publish(exchangeName, exchangeType, routingKey, null, msg);
    }

}
