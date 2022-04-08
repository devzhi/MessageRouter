package com.devzhi.filekafkabridge;

import com.devzhi.filekafkabridge.verticle.ListenVerticle;
import io.vertx.core.Vertx;

/**
 * application entry
 *
 * @author devzhi
 * @date 2022/4/8
 */
public class Application {
    public static void main(String[] args) {
        //创建vertx实例
        Vertx vertx = Vertx.vertx();
        //部署监听Verticle
        vertx.deployVerticle(new ListenVerticle());
    }
}
