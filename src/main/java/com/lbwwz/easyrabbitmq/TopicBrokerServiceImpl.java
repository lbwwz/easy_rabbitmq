package com.lbwwz.easyrabbitmq;


import com.lbwwz.easyrabbitmq.core.Broker;
import com.lbwwz.easyrabbitmq.core.SimpleRabbitAdmin;
import com.lbwwz.easyrabbitmq.util.MqNameUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * @author lbwwz
 */
public class TopicBrokerServiceImpl implements TopicBrokerService,ApplicationContextAware{

    /**
     * 注册的 topic 消息服务中的 broker 容器注册表
     */
    private Map<String,Broker> brokerRegistry;

    private ConnectionFactory connectionFactory;

    private QueueService queueService;


    private SimpleRabbitAdmin simpleRabbitAdmin;

    public TopicBrokerServiceImpl(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
        this.simpleRabbitAdmin = new SimpleRabbitAdmin(connectionFactory);
        this.brokerRegistry = new ConcurrentHashMap<>();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.simpleRabbitAdmin = applicationContext.getBean(SimpleRabbitAdmin.class);
        if(simpleRabbitAdmin == null){
            throw new RuntimeException("simpleRabbitAdmin must be configure in spring-context");
        }
    }


        @Override
    public <T> void publish(String topicName, String tag, T msg) {

            Broker broker = getBroker(topicName);
        //发送消息
        broker.sendMessage(tag,msg);
    }

    private Broker getBroker(String topicName) {
        //get TopicBroker,create it if not exist.
        Broker broker = brokerRegistry.get(MqNameUtil.makeExchangeName(topicName));
        if(Objects.isNull(broker)){
            broker = new TopicBroker(topicName,connectionFactory,simpleRabbitAdmin);
            brokerRegistry.put(topicName,broker);
        }
        return broker;
    }

    /**
     * 监听消息
     * @param topicName 消息名称，用来确定监听的消息对象
     * @param tag 路由键，用于选择性的接收消息
     * @param subscriptionName 监听者的名称，用来映射是生成队列名称
     * @param threadCount 同事监听的线程数
     * @param clazz 消息实体的类型
     * @param msgHandler 消息处理方法
     * @param <T> 消息类型
     */
    @Override
    public <T> void subscribe(String topicName, String tag, String subscriptionName, int threadCount,
                              Class<T> clazz, Consumer<T> msgHandler) {
        if(StringUtils.isBlank(tag)){
            //if without tag,set broker's binding type as fanout.
            tag = "#";
        }
        //todo 确认绑定细节

        Broker broker = getBroker(topicName);

        Queue queue = broker.getRegisteredQueue(tag,subscriptionName);
        if(queue == null){
            //没有注册

        }
        queueService.listen(MqNameUtil.makeQueueName(topicName),threadCount,msgHandler,clazz);
    }




}
