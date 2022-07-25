package com.devzhi.messagerouter.connection.handler;


import com.google.inject.Inject;
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
    @Inject
    private EventBus eventBus;

    /**
     * 连接
     * @return 返回连接后的对象
     */
    public abstract ConnectHandler connect();

    /**
     * 监听
     * @return this
     */
    public abstract ConnectHandler listen();

    /**
     * 注册方法，注册后会自动启动handler
     */
    public void register(){
        this.connect().listen();
    }

    /**
     * 处理接收到的消息
     */
    public void onMessage(String channelAddress,Buffer data){
        StringBuilder addressBuilder = new StringBuilder();
        addressBuilder.append("message.").append(this.getName());
        if (channelAddress != null){
            addressBuilder.append(".").append(channelAddress);
        }
        eventBus.publish(addressBuilder.toString(), data);
    }

    /**
     * 发送消息
     * @param data 消息数据
     * @return 发送结果
     */
    public abstract Boolean sendMessage(Buffer data);
}
