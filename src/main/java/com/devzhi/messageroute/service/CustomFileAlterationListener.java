package com.devzhi.filekafkabridge.service;

import io.vertx.core.Vertx;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationObserver;

import java.io.File;

/**
 * 自定义文件监听器
 *
 * @author devzhi
 * @date 2022/4/8
 */
public class CustomFileAlterationListener extends FileAlterationListenerAdaptor {

    private Vertx vertx;

    public CustomFileAlterationListener(Vertx vertx) {
        //装备Vertx实例对象
        this.vertx = vertx;
    }

    @Override
    public void onStart(FileAlterationObserver observer) {
        super.onStart(observer);
    }

    @Override
    public void onFileCreate(File file) {
        vertx.eventBus().publish("file.listener",file.getPath() + "被创建");
    }

    @Override
    public void onFileChange(File file) {
        super.onFileChange(file);
    }

    @Override
    public void onFileDelete(File file) {
        super.onFileDelete(file);
    }

    @Override
    public void onStop(FileAlterationObserver observer) {
        super.onStop(observer);
    }
}
