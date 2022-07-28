package com.devzhi.messagerouter.connection.connect.impl;

import cn.hutool.core.bean.BeanUtil;
import com.devzhi.messagerouter.connection.connect.AbstractConnectVerticle;
import com.devzhi.messagerouter.model.RabbitMQTarget;
import io.vertx.core.buffer.Buffer;
import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQConsumer;
import io.vertx.rabbitmq.RabbitMQOptions;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
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

    private Map<String, RabbitMQTarget> targetMap;

    public RabbitmqConnectVerticle() {
        this.countDownLatch = new CountDownLatch(1);
    }

    @Override
    public AbstractConnectVerticle connect() {
        // 加载配置文件
        Map<String, Object> options = (Map<String, Object>) getConnectConfig().getConfig().get("options");
        RabbitMQOptions config = BeanUtil.mapToBean(options, RabbitMQOptions.class, false, null);
        this.client = RabbitMQClient.create(vertx, config);

        // 连接
        this.client.start(asyncResult -> {
            if (asyncResult.succeeded()) {
                log.info("[RabbitMQ] {} 连接成功", this.getConnectConfig().getName());
                this.countDownLatch.countDown();
            } else {
                log.warn("[RabbitMQ] {} 连接失败", this.getConnectConfig().getName());
            }
        });

        return this;
    }

    @Override
    public AbstractConnectVerticle listen() {
        // 获取监听队列列表
        List<String> queues = (List<String>) this.getConnectConfig().getConfig().get("queues");
        // 获取发送目标
        Map<String, Map<String, String>> targetTempMap = (Map<String, Map<String, String>>) this.getConnectConfig().getConfig().get("target");
        targetMap = new HashMap<>();
        for (Map.Entry<String, Map<String, String>> entry : targetTempMap.entrySet()) {
            targetMap.put(entry.getKey(),BeanUtil.mapToBean(entry.getValue(),RabbitMQTarget.class,false,null));
        }
        // 监听
        vertx.executeBlocking(handler -> {
            try {
                this.countDownLatch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            // 监听接收
            for (String queue : queues) {
                this.client.basicConsumer(queue, result -> {
                    if (result.succeeded()) {
                        log.info("[RabbitMQ] {} 成功监听队列 {}", this.getConnectConfig().getName(), queue);
                        RabbitMQConsumer consumer = result.result();
                        consumer.handler(message -> {
                            this.onMessage(queue, message.body());
                        });
                    } else {
                        log.warn("[RabbitMQ] {} 监听队列 {} 失败：{}", this.getConnectConfig()
                            .getName(), queue, result.cause());
                    }
                });
            }
            // 监听发送
            for (String channelAddress : targetMap.keySet()) {
                vertx.eventBus().consumer("connect." + this.getConnectConfig().getName() + "." + channelAddress).handler(message -> {
                    vertx.executeBlocking(h -> {
                        this.sendMessage(channelAddress, (Buffer) message.body());
                    });
                });
            }
        });
        return this;
    }

    @Override
    public Boolean sendMessage(String channelAddress, Buffer data) {
        // 获取目标
        RabbitMQTarget target = this.targetMap.get(channelAddress);
        if (target == null) {
            throw new NullPointerException("RabbitMQ目标对象不存在");
        }
        // 发送消息
        client.confirmSelect(confirmResult -> {
            if(confirmResult.succeeded()) {
                client.basicPublish(target.getExchange(), target.getRoutingKey(), data, pubResult -> {
                    if (pubResult.succeeded()) {
                        // 检查消息是否发送成功
                        client.waitForConfirms(waitResult -> {
                            if(waitResult.succeeded()){
                                log.info("[RabbitMQ] 消息发送成功");
                            }else {
                                log.warn("[RabbitMQ] 消息发送失败 {}",waitResult.cause().toString());
                            }
                        });
                    } else {
                        log.warn("[RabbitMQ] 消息发送结果检查失败 {}",pubResult.cause().toString());
                    }
                });
            } else {
                log.warn("[RabbitMQ] 消息发送过程中出错 {}",confirmResult.cause().toString());
            }
        });

        return Boolean.TRUE;
    }
}
