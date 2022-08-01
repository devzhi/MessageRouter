<div align="center">
  <p>
    <h3>
      <b>
        Message Router
      </b>
    </h3>
  </p>
  <p>
    <b>
      高性能消息路由中间件
    </b>
  </p>
  <p>
  <a href="https://github.com/devzhi/MessageRouter/blob/main/LICENSE"><img src="https://img.shields.io/badge/license-APL2.0-blue.svg"></img></a>
  <a href="#"><img src="https://img.shields.io/badge/Contributions-welcome-green?logo=github"></img></a>
    <a href="#"><img src="https://img.shields.io/badge/JDK-1.8+-green.svg"></img></a>
  </p>
</div>

## 愿景

Message Router旨在打造一个适配所有主流消息组件的消息路由中间件

## 安装

`java -jar MessageRouter-0.2.0-fat.jar -conf your_config.json`

## 使用

我们将消息中间件的连接和监听过程以及分发规则进行了抽象，你只需要配置连接信息和转发规则即可实现消息分发。

### 连接信息

#### 公共字段

| 字段名称 | 释义                 |
| -------- | -------------------- |
| name     | 唯一标识名称         |
| type     | 类型，当前仅支持File |

#### Kafka

##### 格式说明

###### 通用配置

| 字段名称              | 释义    |
|-------------------|-------|
| bootstrap.servers | 服务器 |
|topics          | 订阅主题列表  |

###### 生产者配置

| 字段名称 | 释义 |
|------|---------|
|acks | 消息发送可靠性 |

###### 消费者配置

| 字段名称 | 释义      |
|------|---------|
|group.id      | 消费者组ID  |
|auto.offset.reset      | 数据读取策略  |

##### 示例

```json
{
    "name": "k1",
    "type": "Kafka",
    "config": {
        "bootstrap.servers": "localhost:9092",
        "topics": [
            "test1"
        ],
        "producer": {
            "acks": "1"
        },
        "consumer": {
            "group.id": "my_group",
            "auto.offset.reset": "earliest"
        }
    }
}
```

#### RabbitMQ

##### 格式说明

###### 通用配置


| 字段名称 |释义|
| -------- | ---- |
| host | 主机 |
| port | 端口 |
| user | 用户名 |
| password | 密码 |
| virtualHost | 虚拟主机 |
| connectionTimeout | 连接超时时间 |
| requestedHeartbeat | 心跳时间 |
| handshakeTimeout | 握手超时时间 |
| requestedChannelMax | 请求通道最大值 |
| networkRecoveryInterval | 网络恢复间隔 |
| automaticRecoveryEnabled | 启用自动恢复 |

*提示：部分字段不设置也会存在默认值，您并不需要设置每一项，但因精力原因文档尚未完善，如果您有兴趣可以发起PR*

###### 队列配置（用于监听）

传入队列名称即可

###### 目标配置（用于发送）

| 字段名称   | 释义       |
| ---------- | ---------- |
| key        | 唯一标识符 |
| exchange   | 交换机     |
| routingKey | 路由Key    |

##### 示例

```json
{
    "name": "r1",
    "type": "rabbitmq",
    "config": {
        "options": {
            "host": "localhost",
            "port": 5672,
            "user": "guest",
            "password": "guest",
            "virtualHost": "/",
            "connectionTimeout": 6000,
            "requestedHeartbeat": 60,
            "handshakeTimeout": 6000,
            "requestedChannelMax": 5,
            "networkRecoveryInterval": 500,
            "automaticRecoveryEnabled": true
        },
        "queues": [
            "test1"
        ],
        "target": {
            "amq.topic@test1": {
                "exchange": "amq.topic",
                "routingKey": "test1"
            }
        }
    }
}
```

#### 文件

##### 格式说明

| 字段名称 | 释义           |
| -------- | -------------- |
| path     | 监听及写出路径 |

##### 示例

```json
{
    "name": "f1",
    "type": "File",
    "config": {
        "path": "D:/temp/test/f1"
    }
}
```

### 转发规则

#### 格式说明

| 字段名称 | 释义                 |
| -------- | -------------------- |
| source     | 消息源连接名称         |
| target     | 消息目标连接名称 |

#### 示例

```json
{
    "source": "f1",
    "target": "f2"
}
```

### 完整示例

```json
{
    "connection": {
        "connect": [
            {
                "name": "f1",
                "type": "File",
                "config": {
                    "path": "D:/temp/test/f1"
                }
            },
            {
                "name": "f2",
                "type": "File",
                "config": {
                    "path": "D:/temp/test/f2"
                }
            },
            {
                "name": "k1",
                "type": "Kafka",
                "config": {
                    "bootstrap.servers": "localhost:9092",
                    "topics": [
                        "test1"
                    ],
                    "producer": {
                        "acks": "1"
                    },
                    "consumer": {
                        "group.id": "my_group",
                        "auto.offset.reset": "earliest"
                    }
                }
            },
            {
                "name": "r1",
                "type": "rabbitmq",
                "config": {
                    "options": {
                        "host": "localhost",
                        "port": 5672,
                        "user": "guest",
                        "password": "guest",
                        "virtualHost": "/",
                        "connectionTimeout": 6000,
                        "requestedHeartbeat": 60,
                        "handshakeTimeout": 6000,
                        "requestedChannelMax": 5,
                        "networkRecoveryInterval": 500,
                        "automaticRecoveryEnabled": true
                    },
                    "queues": [
                        "test1"
                    ],
                    "target": {
                        "amq.topic@test1": {
                            "exchange": "amq.topic",
                            "routingKey": "test1"
                        }
                    }
                }
            }
        ]
    },
    "route": {
        "routes": [
            {
                "source": "f1",
                "target": "r1.amq.topic@test1"
            },
            {
                "source": "f2",
                "target": "k1.test1"
            },{
                "source": "r1.test1",
                "target": "f2"
            }
        ]
    }
}

```

## 致谢

因为有了下面这些开源项目，才让我们项目的开发和使用过程更加舒适：

- [eclipse-vertx/vert.x: Vert.x is a tool-kit for building reactive applications on the JVM (github.com)](https://github.com/eclipse-vertx/vert.x)
- [dromara/hutool: 🍬A set of tools that keep Java sweet. (github.com)](https://github.com/dromara/hutool)

## Licence

MessageRouter is licensed under the  [Apache License 2.0](https://github.com/devzhi/MessageRouter/blob/main/LICENSE)
