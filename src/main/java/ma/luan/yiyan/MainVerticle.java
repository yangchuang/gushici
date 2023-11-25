package ma.luan.yiyan;

import io.vertx.core.AbstractVerticle;


import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import lombok.extern.slf4j.Slf4j;
import ma.luan.yiyan.api.ApiVerticle;
import ma.luan.yiyan.service.DataService;
import ma.luan.yiyan.service.LogService;


/**
 * @author 乱码 https://luan.ma/
 */
@Slf4j
public class MainVerticle extends AbstractVerticle {

    @Override
    public void start(Promise<Void> startFuture) {
        // 配置 RuntimeError 错误记录
        vertx.exceptionHandler(error -> log.error("未捕获的异常：", error));

        // 顺序部署 Verticle
        Future.<Void>succeededFuture()
                .compose(v -> Future.<String>future(s -> vertx.deployVerticle(new ApiVerticle(), new DeploymentOptions().setConfig(config()), s)))
                .compose(v -> Future.<String>future(s -> vertx.deployVerticle(new DataService(), new DeploymentOptions().setConfig(config()), s)))
                .compose(v -> Future.<String>future(s -> vertx.deployVerticle(new LogService(), new DeploymentOptions().setConfig(config()), s)))
                .onComplete(result -> {
                    if (result.succeeded()) {
                        startFuture.complete();
                    } else {
                        startFuture.fail("Vert.x failed to start");
                    }
                });
    }
}
