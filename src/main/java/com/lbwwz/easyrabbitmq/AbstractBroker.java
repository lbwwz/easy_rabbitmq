package com.lbwwz.easyrabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

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
public abstract   class AbstractBroker implements BeanPostProcessor, Broker, ApplicationContextAware {

    protected ConnectionFactory connectionFactory;

    private ApplicationContext applicationContext;

    /**
     * mq manager
     */
    private RabbitAdmin rabbitAdmin;

    protected Exchange exchange;
    protected RabbitTemplate template;
    protected Map<String, Queue> queueMap;


    protected AbstractBroker(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
        this.queueMap = new ConcurrentHashMap<>();
    }

    protected void setExchange(Exchange exchange) {
        this.exchange = exchange;
        initRabbitTemplate();
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


    public <T> void sendDelayMessage(String routingKey, T msg, int delay, TimeUnit timeUnit) {
        if (delay < 0) {
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

    private String makeQueueName(String messageName) {
        return exchange.getName() + "_" + messageName;
    }

    /**
     * 生成队列
     */
    private void generateQueue(String name) {
        String newQueueName = makeQueueName(name);
        try {
            Queue queue = new Queue(newQueueName, exchange.isDurable());
            this.queueMap.put(newQueueName, queue);

            //将队列和exchange进行绑定
            initBinding(queue);
        } catch (Exception ignore) {
        }
    }

    private void initBinding(Queue queue) {

//        BindingBuilder
        BindingBuilder.bind(queue).to(this.exchange).with("asd");
//        binding.and()

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }


    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        this.rabbitAdmin = applicationContext.getBean(RabbitAdmin.class);
        return bean;
    }
}
