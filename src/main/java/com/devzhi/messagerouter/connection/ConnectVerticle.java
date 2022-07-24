package com.devzhi.messagerouter.connection;

import com.devzhi.messagerouter.connection.handler.ConnectHandler;
import com.devzhi.messagerouter.connection.handler.impl.FileConnectHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

/**
 * 连接Verticle
 *
 * @author devzhi
 */
@Slf4j
public class ConnectVerticle extends AbstractVerticle {
    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        // 获取配置文件
        JsonObject config = vertx.getOrCreateContext().config();
        // 获取连接列表
        JsonArray connectionArray = config.getJsonArray("connection");
        // 依次连接
        for (Object o : connectionArray) {
            // 获取单个连接信息
            JsonObject jsonObject = (JsonObject) o;
            // 反序列化为对应ConnectHandler
            ConnectHandler connectHandler = jsonObject
                .mapTo(ConnectionType.valueOf(jsonObject.getString("type"))
                    .getConnectHandler());
            // 注入事件总线
            connectHandler.setEventBus(vertx.eventBus());
            // 异步执行连接
            CompletableFuture.runAsync(connectHandler::connect);
        }
    }
}
