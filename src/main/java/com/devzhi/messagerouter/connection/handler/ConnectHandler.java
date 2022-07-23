package com.devzhi.messagerouter.connection.handler;


import com.devzhi.messagerouter.model.Message;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.Json;

/**
 * 连接服务接口
 *
 * @author devzhi
 */
public abstract class ConnectHandler {
    /**
     * 连接
     * @return 返回连接后的对象
     */
    public abstract ConnectHandler connect();

    /**
     * 处理接收到的消息
     */
    public void onMessage(EventBus eventBus, Message message){
        eventBus.publish("message", Json.encode(message));
    }

    /**
     * 发送消息
     * @param message 文本消息
     * @return 发送结果
     */
    public abstract Boolean sendMessage(String message);
}
