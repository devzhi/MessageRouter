package com.devzhi.messagerouter.connection;

import cn.hutool.core.util.ReflectUtil;
import com.devzhi.messagerouter.connection.handler.ConnectHandler;
import com.devzhi.messagerouter.connection.handler.impl.FileConnectHandler;
import lombok.Getter;

/**
 * 连接类型枚举类
 *
 * @author devzhi
 */
@Getter
public enum ConnectionType {


    /**
     * 文件连接处理器
     */
    File("File",FileConnectHandler.class);

    private String type;
    private Class<? extends ConnectHandler> connectHandler;

    ConnectionType(String type, Class<? extends ConnectHandler> connectHandler) {
        this.type = type;
        this.connectHandler = connectHandler;
    }
}
