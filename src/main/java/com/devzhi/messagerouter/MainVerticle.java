package com.devzhi.messagerouter;

import com.devzhi.messagerouter.connection.ConnectVerticle;
import com.devzhi.messagerouter.route.RouteVerticle;
import io.vertx.config.ConfigRetriever;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MainVerticle extends AbstractVerticle {

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        // 加载配置文件
        ConfigRetriever retriever = ConfigRetriever.create(vertx);
        // 部署Verticle
        retriever.getConfig(json -> {
            vertx.deployVerticle(new ConnectVerticle(),
                new DeploymentOptions().setConfig(json.result().getJsonObject("connect")))
                .onFailure(hander -> {
                log.error("[模块部署]连接模块部署失败:({}",hander.getCause().toString());
                });
            vertx.deployVerticle(new RouteVerticle(),
                new DeploymentOptions().setConfig(json.result().getJsonObject("route"))).onFailure(hander -> {
                log.error("[模块部署]路由模块部署失败:(\n{}",hander.getCause().toString());
            });;
        });
    }
}
