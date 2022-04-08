package com.devzhi.filekafkabridge;

import com.devzhi.filekafkabridge.verticle.ListenVerticle;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;

/**
 * application entry
 *
 * @author devzhi
 * @date 2022/4/8
 */
@Slf4j
public class Application {
    public static void main(String[] args) {
        //创建vertx实例
        Vertx vertx = Vertx.vertx();
        //部署监听Verticle
        vertx.deployVerticle(new ListenVerticle());
    }
}
