package com.devzhi.messagerouter.connection.connect.impl;

import com.devzhi.messagerouter.connection.connect.AbstractConnectVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class KafkaConnectVerticle extends AbstractConnectVerticle {


    /**
     * 消费者
     */
    private KafkaConsumer<String, String> consumer;

    /**
     * 生产者
     */
    private KafkaProducer<String, String> producer;

    @Override
    public AbstractConnectVerticle connect() {
        // 创建消费者
        Map<String, String> consumerConfig = new HashMap<>();
        consumerConfig.put("bootstrap.servers", (String) getConnectConfig().getConfig().get("bootstrap.servers"));
        consumerConfig.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        consumerConfig.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        consumerConfig.put("enable.auto.commit", "true");
        consumerConfig.putAll((Map<String, String>) this.getConnectConfig().getConfig().get("consumer"));

        // 创建生产者
        Map<String, String> producerConfig = new HashMap<>();
        producerConfig.put("bootstrap.servers", (String) getConnectConfig().getConfig().get("bootstrap.servers"));
        producerConfig.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        producerConfig.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        producerConfig.putAll((Map<String, String>) this.getConnectConfig().getConfig().get("producer"));
        // 使用消费者和 Apache Kafka 交互
        this.consumer = KafkaConsumer.create(vertx, consumerConfig);
        // 使用生产者和 Apache Kafka 交互
        this.producer = KafkaProducer.create(vertx, producerConfig);
        return this;
    }

    @Override
    public AbstractConnectVerticle listen() {
        // 加载topics
        Set<String> topics = new HashSet<>((ArrayList<String>) getConnectConfig().getConfig().get("topics"));
        // 订阅
        log.info("[Kafka]订阅主题：{}",topics);
        consumer.subscribe(topics);
        // 监听接收
        consumer.handler(record -> {
            this.onMessage(record.topic(), Buffer.buffer(record.value()));
            log.info("[Kafka]接收到消息：topic:{},offset:{},partition:{},timestamp:{}",record.topic(),record.offset(),
                record.partition(),record.timestamp());
        });
        // 监听发送
        for (String topic : topics) {
            getVertx().eventBus().consumer("connect." + getConnectConfig().getName() + "." + topic).handler(message -> {
                this.sendMessage(topic,(Buffer)message.body());
            });
        }
        return this;
    }

    @Override
    public Boolean sendMessage(String channelAddress, Buffer data) {
        if (channelAddress == null) {
            throw new NullPointerException("目标主题不能为空");
        }
        producer.send(KafkaProducerRecord.create(channelAddress, data.toString())).onSuccess(handler -> {
            log.info("[Kafka]消息发送成功 {}", handler.toJson());
        }).onFailure(handler -> {
            log.warn("[Kafka]消息发送失败 {}", handler.getCause().toString());
        });
        return true;
    }

}
