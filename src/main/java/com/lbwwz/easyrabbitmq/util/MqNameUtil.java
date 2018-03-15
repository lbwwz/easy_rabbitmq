package com.lbwwz.easyrabbitmq.util;

/**
 * @author lbwwz
 */
public class MqNameUtil {

    /**
     * 构造Exchange名称
     * @param msgName 消息名
     * @return
     */
    public static String makeExchangeName(String msgName){
            return "_exchange_"+msgName;
    }

    public static String makeQueueName(String msgName){
        return "_queue_"+msgName;
    }
}
