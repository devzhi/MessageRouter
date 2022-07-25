package com.devzhi.messagerouter.connection.connect.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.io.watch.WatchMonitor;
import cn.hutool.core.io.watch.Watcher;
import cn.hutool.core.io.watch.watchers.DelayWatcher;
import cn.hutool.core.util.IdUtil;
import com.devzhi.messagerouter.connection.connect.AbstractConnectVerticle;
import com.devzhi.messagerouter.model.ConnectConfig;
import io.vertx.core.buffer.Buffer;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.util.concurrent.CompletableFuture;

/**
 * 文件系统处理器
 *
 * @author devzhi
 * @date 2022/7/25
 */
@Slf4j
public class FileConnectVerticle extends AbstractConnectVerticle {

    /**
     * 路径监听器
     */
    private WatchMonitor monitor;

    @Override
    public AbstractConnectVerticle connect() {
        // 监听创建事件
        monitor = WatchMonitor.create((String) this.getConnectConfig().getConfig().get("path"), WatchMonitor.ENTRY_MODIFY);
        return this;
    }

    @Override
    public AbstractConnectVerticle listen() {
        // 配置监听转发
        AbstractConnectVerticle that = this;
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
                    that.onMessage(null, Buffer.buffer().appendBytes(FileUtil.readBytes(filePath)));
                }

                @Override
                public void onDelete(WatchEvent<?> watchEvent, Path path) {
                }

                @Override
                public void onOverflow(WatchEvent<?> watchEvent, Path path) {
                }
            }, 500));
            log.info("[文件系统连接]开始监听目录：{}", this.getConnectConfig().getConfig().get("path"));
            monitor.start();
        });
        // 监听对应地址并执行保存操作
        getVertx().eventBus().consumer("connect." + this.getConnectConfig().getName()).handler(message -> {
            CompletableFuture.runAsync(() -> {
                this.sendMessage((Buffer) message.body());
            });
        });
        return this;
    }

    @Override
    public Boolean sendMessage(Buffer data) {
        // 利用雪花算法创建一个唯一的TXT文件
        File file = new File(this.getConnectConfig().getConfig().get("path") + "/" + IdUtil.getSnowflakeNextIdStr() + ".mrd");
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
