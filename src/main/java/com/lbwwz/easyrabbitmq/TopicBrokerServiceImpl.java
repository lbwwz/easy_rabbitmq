package com.lbwwz.easyrabbitmq;



import org.springframework.amqp.rabbit.connection.ConnectionFactory;

import java.util.Map;
import java.util.Objects;

/**
 * @author lbwwz
 */
public class TopicBrokerServiceImpl implements TopicBrokerService{
    private Map<String,Broker> brokerMap;

    private ConnectionFactory connectionFactory;

    public TopicBrokerServiceImpl(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public void publish(String topicName) {
        //先创建 TopicBroker
        Broker broker = brokerMap.get("");
        if(Objects.isNull(broker)){
            broker = new TopicBroker(connectionFactory);
            brokerMap.put("",broker);
        }
        //发送逻辑

    }

    @Override
    public void subscribe() {

    }
}
