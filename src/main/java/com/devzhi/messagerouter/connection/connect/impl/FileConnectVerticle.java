package com.devzhi.messagerouter.connection.connect.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.io.watch.WatchMonitor;
import cn.hutool.core.io.watch.Watcher;
import cn.hutool.core.io.watch.watchers.DelayWatcher;
import cn.hutool.core.util.IdUtil;
import com.devzhi.messagerouter.connection.connect.AbstractConnectVerticle;
import io.vertx.core.buffer.Buffer;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.WatchEvent;

/**
 * File处理器
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
        vertx.executeBlocking(handler -> {
            monitor.setWatcher(new DelayWatcher(new Watcher() {
                @Override
                public void onCreate(WatchEvent<?> watchEvent, Path path) {
                }

                @Override
                public void onModify(WatchEvent<?> watchEvent, Path path) {
                    // 监听到新文件后以UTF8编码的形式读取数据并调用onMessage事件
                    String filePath = path + "/" + watchEvent.context();
                    log.info("[File]发现文件 {}", filePath);
                    that.onMessage(null, Buffer.buffer().appendBytes(FileUtil.readBytes(filePath)));
                }

                @Override
                public void onDelete(WatchEvent<?> watchEvent, Path path) {
                }

                @Override
                public void onOverflow(WatchEvent<?> watchEvent, Path path) {
                }
            }, 500));
            log.info("[File]开始监听目录：{}", this.getConnectConfig().getConfig().get("path"));
            monitor.start();
        });
        // 监听对应地址并执行保存操作
        getVertx().eventBus().consumer("connect." + this.getConnectConfig().getName()).handler(message -> {
            vertx.executeBlocking(handler -> {
                // TODO 文件加入channelAddress
                if (this.sendMessage(null,(Buffer) message.body())) {
                    handler.complete(true);
                }else {
                    handler.complete(false);
                }
            });
        });
        return this;
    }

    @Override
    public Boolean sendMessage(String channelAddress,Buffer data) {
        // 构造filename
        StringBuilder filename = new StringBuilder();
        // 基本路径
        filename.append(this.getConnectConfig().getConfig().get("path")).append("/");
        // 若有通道则写入通道
        if (channelAddress != null){
            filename.append(channelAddress).append(".");
        }
        // 利用雪花算法创建一个唯一的TXT文件
        filename.append(IdUtil.getSnowflakeNextIdStr()).append(".mrd");
        File file = new File(filename.toString());
        try {
            // 写入数据
            FileUtil.writeBytes(data.getBytes(), file);
            log.info("[File]写入成功 {}", file.getPath());
        } catch (IORuntimeException e) {
            // 简单打印出错误信息
            log.warn("[File]写入失败 {}", e.getCause().toString());
            return false;
        }
        return true;
    }
}
