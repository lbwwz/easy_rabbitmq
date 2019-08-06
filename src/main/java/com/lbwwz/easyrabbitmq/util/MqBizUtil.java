package com.lbwwz.easyrabbitmq.util;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Method;
import com.rabbitmq.client.ShutdownSignalException;

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
            queueName.append("/");
            queueName.append(arg);
        }
        return queueName.toString();
    }

    public static boolean isNormalShutdown(ShutdownSignalException sig) {
        Method shutdownReason = sig.getReason();
        return shutdownReason instanceof AMQP.Connection.Close
            && AMQP.REPLY_SUCCESS == ((AMQP.Connection.Close) shutdownReason).getReplyCode()
            && "OK".equals(((AMQP.Connection.Close) shutdownReason).getReplyText());
    }

    // todo 详尽的拓展
    public void shutdownCompleted(ShutdownSignalException cause) {
        if (cause.isHardError()) {
            Connection conn = (Connection)cause.getReference();
            if (!cause.isInitiatedByApplication()) {
                Method reason = cause.getReason();
            }
        } else { Channel ch = (Channel)cause.getReference(); }
    }



}
