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

`java -jar MessageRouter-0.1.1-fat.jar -conf your_config.json`

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

##### 通用配置

| 字段名称              | 释义    |
|-------------------|-------|
| bootstrap.servers | 服务器 |
|topics          | 订阅主题列表  |

##### 生产者配置

| 字段名称 | 释义 |
|------|---------|
|acks | 消息发送可靠性 |

##### 消费者配置

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
            }
        ]
    },
    "route": {
        "routes": [
            {
                "source": "k1.test1",
                "target": "f1"
            },
            {
                "source": "f2",
                "target": "k1.test1"
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
