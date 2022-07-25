package com.devzhi.messagerouter.connection;

import cn.hutool.core.util.ReflectUtil;
import com.devzhi.messagerouter.connection.connect.AbstractConnectVerticle;
import com.devzhi.messagerouter.connection.connect.impl.FileConnectVerticle;
import com.devzhi.messagerouter.model.ConnectConfig;
import lombok.Getter;

/**
 * 连接类型枚举类
 *
 * @author devzhi
 */
public enum ConnectionType {


    /**
     * 文件连接处理器
     */
    File("File", FileConnectVerticle.class);

    private String type;
    private Class<? extends AbstractConnectVerticle> connectVerticleClass;

    ConnectionType(String type, Class<? extends AbstractConnectVerticle> connectVerticleClass) {
        this.type = type;
        this.connectVerticleClass = connectVerticleClass;
    }

    public AbstractConnectVerticle getConnectVerticle(ConnectConfig config){
        AbstractConnectVerticle connectVerticle = ReflectUtil.newInstance(connectVerticleClass);
        connectVerticle.setConnectConfig(config);
        return connectVerticle;
    }
}
