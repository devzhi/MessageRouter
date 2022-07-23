package com.devzhi.messageroute.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 消息类
 *
 * @author devzhi
 */
@Data
@AllArgsConstructor
public class Message {
    /**
     * 连接名称
     */
    private String connectName;

    /**
     * 数据
     */
    private String data;
}
