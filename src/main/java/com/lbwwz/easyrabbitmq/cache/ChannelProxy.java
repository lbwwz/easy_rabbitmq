package com.lbwwz.easyrabbitmq.cache;

import com.rabbitmq.client.Channel;

/**
 * @author lbwwz
 */
public interface ChannelProxy extends Channel {
    /**
     *
     * 当前信道是否支持事务
     * @return
     */
    boolean isTransactional();
}
