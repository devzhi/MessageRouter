package com.devzhi.messagerouter.model;

import lombok.Data;

import java.util.Map;

/**
 * 连接配置类
 *
 * @author devzhi
 * @date 2022/7/25
 */
@Data
public class ConnectConfig {
    private String name;
    private String type;
    private Map<String,Object> config;
}
