package com.lbwwz.easyrabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * @author lbwwz
 */
public class QueueServiceImpl implements QueueService {

    private final static int LISTEN_THREAD_COUNT = 3;

    private static final Logger LOGGER = LoggerFactory.getLogger(QueueServiceImpl.class);

    private AbstractBrokerMessageProcessImpl abstractBrokerServiceImpl;




    @Override
    public <T> void listen(String queueName, Consumer<String> msgHandler) {


        listen(queueName, LISTEN_THREAD_COUNT, msgHandler, String.class);
    }

    @Override
    public <T> void listen(String queueName, int threadCount, Consumer<String> msgHandler) {

        listen(queueName, threadCount, msgHandler, String.class);
    }

    //对每一个监听，都维护一个connection 和 channelList的 对应组合
    @Override
    public <T> void listen(String queueName, int threadCount, Consumer<T> msgHandler, Class<T> clazz) {

        //abstractBrokerServiceImpl.consume();

    }

}
