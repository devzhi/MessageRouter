package com.devzhi.messagerouter.connection.handler.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.io.watch.WatchMonitor;
import cn.hutool.core.io.watch.Watcher;
import cn.hutool.core.util.IdUtil;
import com.devzhi.messagerouter.connection.handler.ConnectHandler;
import com.devzhi.messagerouter.model.Message;
import io.vertx.core.eventbus.EventBus;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.WatchEvent;


/**
 * 文件连接处理器
 *
 * @author devzhi
 */
@Slf4j
public class FileConnectHandler extends ConnectHandler {


    /**
     * 连接名称
     */
    private final String name;

    /**
     * 操作路径
     */
    private final String path;

    /**
     * 消息总线
     */
    private final EventBus eventBus;

    public FileConnectHandler(String name,EventBus eventBus,String path){
        this.name = name;
        this.eventBus = eventBus;
        this.path = path;
    }

    @Override
    public ConnectHandler connect() {
        // 监听创建事件
        WatchMonitor monitor = WatchMonitor.create(this.path, WatchMonitor.ENTRY_CREATE);
        // 配置监听转发
        ConnectHandler that = this;
        monitor.setWatcher(new Watcher() {
            @Override
            public void onCreate(WatchEvent<?> watchEvent, Path path) {
                // 监听到新文件后以UTF8编码的形式读取数据并调用onMessage事件
                log.info("[发现文件]： {}",path.toString());
                that.onMessage(eventBus,new Message(name,FileUtil.readUtf8String(path.toFile())));
            }

            @Override
            public void onModify(WatchEvent<?> watchEvent, Path path) {
            }

            @Override
            public void onDelete(WatchEvent<?> watchEvent, Path path) {
            }

            @Override
            public void onOverflow(WatchEvent<?> watchEvent, Path path) {
            }
        });
        return this;
    }

    @Override
    public Boolean sendMessage(String message) {
        // 利用雪花算法创建一个唯一的TXT文件
        File file = new File(path + "/" + IdUtil.getSnowflakeNextIdStr() + ".txt");
        try {
            // 写入数据
            FileUtil.writeString(message,file, StandardCharsets.UTF_8);
            log.info("[写入成功]： {}",file.getPath());
        }catch (IORuntimeException e){
            // 简单打印出错误信息
            log.warn("[写入失败]： {}",e.getMessage());
            return false;
        }
        return true;
    }
}
