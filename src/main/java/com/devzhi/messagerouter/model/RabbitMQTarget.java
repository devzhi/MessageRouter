package com.devzhi.messagerouter.model;

import lombok.Data;

/**
 * RabbitMQ目标描述类
 *
 * @author devzhi
 */
@Data
public class RabbitMQTarget {
    /**
     * 交换机
     */
    private String exchange;

    /**
     * 路由Key
     */
    private String routingKey;
}
