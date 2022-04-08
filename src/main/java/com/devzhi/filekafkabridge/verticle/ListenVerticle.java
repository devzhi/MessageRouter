package com.devzhi.filekafkabridge.verticle;

import com.devzhi.filekafkabridge.service.CustomFileAlterationListener;
import io.vertx.core.AbstractVerticle;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 文件监听Verticle
 *
 * @author devzhi
 * @date 2022/4/8
 */
@Slf4j
public class ListenVerticle extends AbstractVerticle {
    @Override
    public void start() throws Exception {
        //获取监听目录(模拟)
        List<String> pathList = Arrays.asList("D:/MyProject/FileKafkaBridge/temp");
        //注册监听目录
        long interval = TimeUnit.SECONDS.toMillis(1);
        //加载Observer
        List<FileAlterationObserver> observerList = new ArrayList<>();
        for (String path : pathList) {
            FileAlterationObserver observer = new FileAlterationObserver(path);
            observer.addListener(new CustomFileAlterationListener(vertx));
            observerList.add(observer);
        }
        FileAlterationMonitor monitor = new FileAlterationMonitor(interval);
        for (FileAlterationObserver observer : observerList) {
            monitor.addObserver(observer);
        }
        //启动文件监听器
        monitor.start();
        //监听事件
        vertx.eventBus().consumer("file.listener",msg -> {
            System.out.println(msg.body());
        });
    }
}
