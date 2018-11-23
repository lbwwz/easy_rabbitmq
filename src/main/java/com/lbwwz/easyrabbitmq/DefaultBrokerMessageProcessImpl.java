package com.lbwwz.easyrabbitmq;

/**
 *
 * 具体的消息推送和接收的服务类
 * @author lbwwz
 */
public class DefaultBrokerMessageProcessImpl extends AbstractBrokerMessageProcessImpl {

    public DefaultBrokerMessageProcessImpl(String host, String userName, String password, String vHost) {
        super(host, userName, password, vHost);
    }
}
