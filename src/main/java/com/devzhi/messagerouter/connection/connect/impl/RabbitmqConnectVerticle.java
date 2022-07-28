package com.devzhi.messagerouter.connection.connect.impl;

import cn.hutool.core.bean.BeanUtil;
import com.devzhi.messagerouter.connection.connect.AbstractConnectVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQConsumer;
import io.vertx.rabbitmq.RabbitMQOptions;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
     * RabbitMQ处理器
 *
 * @author devzhi
 */
@Slf4j
public class RabbitmqConnectVerticle extends AbstractConnectVerticle {

    /**
     * RabbitMQ客户端
     */
    private RabbitMQClient client;

    /**
     * 计数锁，确保连接成功后再监听队列防止监听失败
     */
    private final CountDownLatch countDownLatch;

    public RabbitmqConnectVerticle(){
        this.countDownLatch = new CountDownLatch(1);
    }

    @Override
    public AbstractConnectVerticle connect() {
        // 加载配置文件
        Map<String, Object> options = (Map<String, Object>)getConnectConfig().getConfig().get("options");
        RabbitMQOptions config = BeanUtil.mapToBean(options, RabbitMQOptions.class, false, null);
        this.client = RabbitMQClient.create(vertx, config);

        // 连接
        this.client.start(asyncResult -> {
            if (asyncResult.succeeded()) {
                log.info("[RabbitMQ] {} 连接成功",this.getConnectConfig().getName());
                this.countDownLatch.countDown();
            } else {
                log.warn("[RabbitMQ] {} 连接失败",this.getConnectConfig().getName());
            }
        });

        return this;
    }

    @Override
    public AbstractConnectVerticle listen() {
        List<String> queues = (List<String>) this.getConnectConfig().getConfig().get("queues");
        vertx.executeBlocking(handler -> {
            try {
                this.countDownLatch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            for (String queue : queues) {
                this.client.basicConsumer(queue,result -> {
                    if (result.succeeded()){
                        log.info("[RabbitMQ] {} 成功监听队列 {}",this.getConnectConfig().getName(),queue);
                        RabbitMQConsumer consumer = result.result();
                        consumer.handler(message -> {
                            this.onMessage(queue,message.body());
                        });
                    }else {
                        log.warn("[RabbitMQ] {} 监听队列 {} 失败：{}",this.getConnectConfig()
                            .getName(),queue,result.cause());
                    }
                });
            }
        });
        return this;
    }

    @Override
    public Boolean sendMessage(String channelAddress, Buffer data) {
        // TODO
        return null;
    }
}
