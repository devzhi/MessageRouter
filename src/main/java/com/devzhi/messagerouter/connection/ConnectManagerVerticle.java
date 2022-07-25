package com.devzhi.messagerouter.connection;

import com.devzhi.messagerouter.connection.connect.AbstractConnectVerticle;
import com.devzhi.messagerouter.model.ConnectConfig;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

/**
 * 连接Verticle
 *
 * @author devzhi
 */
@Slf4j
public class ConnectManagerVerticle extends AbstractVerticle {
    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        // 获取配置文件
        JsonObject config = vertx.getOrCreateContext().config();
        // 获取连接列表
        JsonArray connectArray = config.getJsonArray("connect");
        // 依次连接
        for (Object o : connectArray) {
            // 获取单个连接信息
            JsonObject jsonObject = (JsonObject) o;
            // 反序列化为对应ConnectHandler
            ConnectConfig connectConfig = jsonObject.mapTo(ConnectConfig.class);
            // 部署连接处理器
            vertx.deployVerticle(ConnectionType.valueOf(connectConfig.getType()).getConnectVerticle(connectConfig));
        }
    }
}
