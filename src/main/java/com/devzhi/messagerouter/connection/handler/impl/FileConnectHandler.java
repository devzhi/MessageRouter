package com.devzhi.messagerouter.connection.handler.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.io.watch.WatchMonitor;
import cn.hutool.core.io.watch.Watcher;
import cn.hutool.core.io.watch.watchers.DelayWatcher;
import cn.hutool.core.util.IdUtil;
import com.devzhi.messagerouter.connection.handler.ConnectHandler;
import io.vertx.core.buffer.Buffer;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.util.concurrent.CompletableFuture;


/**
 * 文件连接处理器
 *
 * @author devzhi
 */
@Slf4j
@Getter
@Setter
public class FileConnectHandler extends ConnectHandler {

    /**
     * 操作路径
     */
    private String path;


    @Override
    public ConnectHandler connect() {
        // 监听创建事件
        WatchMonitor monitor = WatchMonitor.create(this.path, WatchMonitor.ENTRY_MODIFY);
        // 配置监听转发
        ConnectHandler that = this;
        CompletableFuture.runAsync(() -> {
            monitor.setWatcher(new DelayWatcher(new Watcher() {
                @Override
                public void onCreate(WatchEvent<?> watchEvent, Path path) {
                }

                @Override
                public void onModify(WatchEvent<?> watchEvent, Path path) {
                    // 监听到新文件后以UTF8编码的形式读取数据并调用onMessage事件
                    String filePath = path + "/" + watchEvent.context();
                    log.info("[文件系统连接]发现文件 {}", filePath);
                    that.onMessage(Buffer.buffer().appendBytes(FileUtil.readBytes(filePath)));
                }

                @Override
                public void onDelete(WatchEvent<?> watchEvent, Path path) {
                }

                @Override
                public void onOverflow(WatchEvent<?> watchEvent, Path path) {
                }
            }, 500));
            log.info("[文件系统连接]开始监听目录：{}", this.path);
            monitor.start();
        });
        // 监听对应地址并执行保存操作
        this.getEventBus().consumer("connect." + this.getName()).handler(message -> {
            CompletableFuture.runAsync(() -> {
                this.sendMessage((Buffer) message.body());
            });
        });
        return this;
    }

    @Override
    public Boolean sendMessage(Buffer data) {
        // 利用雪花算法创建一个唯一的TXT文件
        File file = new File(path + "/" + IdUtil.getSnowflakeNextIdStr() + ".mrd");
        try {
            // 写入数据
            FileUtil.writeBytes(data.getBytes(), file);
            log.info("[文件系统连接]写入成功 {}", file.getPath());
        } catch (IORuntimeException e) {
            // 简单打印出错误信息
            log.warn("[文件系统连接]写入失败 {}", e.getCause().toString());
            return false;
        }
        return true;
    }
}
