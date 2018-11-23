package com.lbwwz.easyrabbitmq.util;

/**
 * @author lbwwz
 */
public class MqBizUtil {

    private final static String EXCHANGE_TAG = "_exchange_";
    private final static String QUEUE_TAG = "_queue_";

    /**
     * 构造Exchange名称
     *
     * @param msgName 消息名
     * @return
     */
    public static String makeExchangeName(String msgName, String... args) {
        return makeDestinationName(EXCHANGE_TAG, msgName, args);
    }

    public static String makeQueueName(String msgName, String... args) {
        return makeDestinationName(QUEUE_TAG, msgName, args);
    }

    private static String makeDestinationName(String destinationTag, String msgName, String... args) {
        StringBuilder queueName = new StringBuilder(destinationTag);
        queueName.append(msgName);
        for (String arg : args) {
            queueName.append(arg);
        }
        return queueName.toString();
    }



}
