package com.lbwwz.easyrabbitmq.core;

import com.lbwwz.easyrabbitmq.util.MqNameUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * middleware broker
 * 消息在middleware中对应的映射与操作类
 * <p>
 * <p>初始化一个（类）消息对应的 exchange,  queue(s) </p>
 * <p>声明exchange，queue(s) 之间的绑定关系</p>
 *
 * @author lbwwz
 */
public abstract class AbstractBroker implements Broker {

    Logger logger = LoggerFactory.getLogger(Broker.class);

    protected ConnectionFactory connectionFactory;

    /**
     * mq manager
     */
    private SimpleRabbitAdmin admin;

    protected Exchange exchange;
    public RabbitTemplate template;
    protected Map<String, Queue> queueRegistry;

    @Override
    public Queue getRegisteredQueue(String tag, String listenerName) {
        return queueRegistry.get(tag+"_"+listenerName);
    }


    protected AbstractBroker(ConnectionFactory connectionFactory,SimpleRabbitAdmin admin) {
        this.connectionFactory = connectionFactory;
        this.admin = admin;
        this.queueRegistry = new ConcurrentHashMap<>();
    }

    protected void setExchange(Exchange exchange) {
        if (exchange == null){
            return;
        }
        //only init temple once
        if(this.exchange == null){
            this.exchange = exchange;
            admin.declareExchange(exchange);
            initRabbitTemplate();
        }
    }

    /**
     * 初始化 template
     */
    private void initRabbitTemplate() {
        this.template = new RabbitTemplate(connectionFactory);
        //设置exchange 转换器
        template.setMessageConverter(new Jackson2JsonMessageConverter());
        template.setMandatory(true);
        template.setExchange(exchange.getName());
    }

    @Override
    public <T> void sendDelayMessage(String routingKey, T msg, int delay, TimeUnit timeUnit) {
        if (delay <= 0) {
            template.convertAndSend(routingKey, msg);
            return;
        }
        try {
            delay = (int) timeUnit.toMillis(delay);
        } catch (Exception ex) {
            delay = Integer.MAX_VALUE;
        }
        int xdelay = delay;
        template.convertAndSend(routingKey, msg, message -> {
            message.getMessageProperties().setDelay(xdelay);
            return message;
        });
    }

    @Override
    public <T> void sendMessage(String routingKey, T msg) {
            template.convertAndSend(routingKey, msg);
    }

    /**
     * 注册并绑定消息队列
     * <p>注册监听的服务，若不存在队列，则需要先注册队列，并将其与exchange进行绑定</p>
     */
    public void generateAndBindQueue(String name,String routeKey) {
        String newQueueName = MqNameUtil.makeQueueName(name);
        //先检验队列是否存在，若不存在，则定义队列
        try {
            Queue queue = QueueBuilder.durable(newQueueName).build();
            //将队列和exchange进行绑定
            Binding binding = BindingBuilder.bind(queue).to(this.exchange).with(routeKey).noargs();
            this.queueRegistry.put(newQueueName, queue);
        } catch (Exception ex) {
            logger.info("出现异常，表示该队列已经被注册",ex);
        }
    }

}
