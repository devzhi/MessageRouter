package com.devzhi.messagerouter.connection.handler;


import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.Json;
import lombok.Data;

import java.nio.ByteBuffer;

/**
 * 连接服务接口
 *
 * @author devzhi
 */
@Data
public abstract class ConnectHandler {

    /**
     * 连接名称
     */
    private String name;

    /**
     * 类型
     */
    private String type;

    /**
     * 消息总线
     */
    private EventBus eventBus;

    /**
     * 连接
     * @return 返回连接后的对象
     */
    public abstract ConnectHandler connect();

    /**
     * 处理接收到的消息
     */
    public void onMessage(EventBus eventBus, Buffer data){
        eventBus.publish("message."+this.name, data);
    }

    /**
     * 发送消息
     * @param message 文本消息
     * @return 发送结果
     */
    public abstract Boolean sendMessage(Buffer data);
}
