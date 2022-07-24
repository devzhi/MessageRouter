package com.devzhi.messagerouter.model;

import io.vertx.core.buffer.Buffer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 消息类
 *
 * @author devzhi
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message {
    /**
     * 连接名称
     */
    private String connectName;

    /**
     * 数据
     */
    private Buffer data;
}
