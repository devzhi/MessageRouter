package com.devzhi.messagerouter.connection.connect;

import com.devzhi.messagerouter.model.ConnectConfig;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import lombok.Getter;
import lombok.Setter;

/**
 * @author devzhi
 * @date 2022/7/25
 */
public abstract class AbstractConnectVerticle extends AbstractVerticle {

    /***
     * 连接配置
     */
    @Getter
    @Setter
    private ConnectConfig connectConfig;

    /**
     * 连接
     * @return 返回连接后的对象
     */
    public abstract AbstractConnectVerticle connect();

    /**
     * 监听
     * @return this
     */
    public abstract AbstractConnectVerticle listen();

    /**
     * 发送消息
     * @param data 消息数据
     * @return 发送结果
     */
    public abstract Boolean sendMessage(String channelAddress,Buffer data);

    @Override
    public void start(Promise<Void> startPromise) {
        this.connect().listen();
        startPromise.complete();
    }

    /**
     * 处理接收到的消息
     */
    public void onMessage(String channelAddress, Buffer data){
        StringBuilder addressBuilder = new StringBuilder();
        addressBuilder.append("message.").append(this.getConnectConfig().getName());
        if (channelAddress != null){
            addressBuilder.append(".").append(channelAddress);
        }
        getVertx().eventBus().publish(addressBuilder.toString(), data);
    }
}
