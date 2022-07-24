package com.devzhi.messagerouter.route;

import com.devzhi.messagerouter.model.Route;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 路由Verticle
 *
 * @author devzhi
 */
@Slf4j
public class RouteVerticle extends AbstractVerticle {

    /**
     * 路由表
     */
    private Map<String, List<Route>> routeMap = new HashMap<>();

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        // 获取配置文件
        JsonObject config = vertx.getOrCreateContext().config();
        // 获取路由列表
        JsonArray routeArray = config.getJsonArray("routes");
        for (Object o : routeArray) {
            Route route = ((JsonObject) o).mapTo(Route.class);
            List<Route> routes = routeMap.get(route.getSource());
            if (routes == null){
                routes = new ArrayList<>();
                routes.add(route);
                routeMap.put(route.getSource(),routes);
            }else {
                routes.add(route);
            }
        }
        // 监听消息事件
        for (String source : routeMap.keySet()) {
            vertx.eventBus().consumer("message."+source,message -> {
                // 获取路由集合
                List<Route> routes = routeMap.get(source);
                if (routes != null){
                    // 转发消息
                    for (Route route : routes) {
                        Buffer data = (Buffer) message.body();
                        vertx.eventBus().publish("connect."+route.getTarget(),data);
                    }
                }
            });
        }
    }
}
