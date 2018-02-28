package com.lbwwz.easyrabbitmq;



import com.lbwwz.easyrabbitmq.core.Broker;
import com.lbwwz.easyrabbitmq.core.SimpleRabbitAdmin;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
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
        //get TopicBroker,create it if not exist.
        Broker broker = brokerRegistry.get(getExchangeName(topicName));
        if(Objects.isNull(broker)){
            broker = new TopicBroker(topicName,connectionFactory,simpleRabbitAdmin);
            brokerRegistry.put(topicName,broker);
        }
        //发送消息
        broker.sendMessage(tag,msg);
    }

    @Override
    public <T> void subscribe(String topicName, String tag, String subscriptionName, int threadCount, Class<T> clazz, Consumer<T> msgHandler) {
        if(StringUtils.isBlank(tag)){
            //if without tag,set broker's binding type as fanout.
            tag = "#";
        }

    }

    private String getExchangeName(String topicName){
        return topicName+"_exchange";
    }


}
