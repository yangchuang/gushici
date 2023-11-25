package ma.luan.yiyan.service;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisAPI;
import io.vertx.redis.client.Response;
import lombok.extern.slf4j.Slf4j;
import ma.luan.yiyan.constants.Key;
import ma.luan.yiyan.util.OptionsUtil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Slf4j
public class LogService extends AbstractVerticle {
    private RedisAPI redisAPI;

    @Override
    public void start(Promise<Void> startPromise) {
        vertx.eventBus().consumer(Key.SET_HISTORY_TO_REDIS, this::setHistoryToRedis);
        vertx.eventBus().consumer(Key.GET_HISTORY_FROM_REDIS, this::getHistoryFromRedis);
        Redis redis = Redis.createClient(vertx, OptionsUtil.getRedisOptions(config()));
        redisAPI = RedisAPI.api(redis);
        startPromise.complete();
    }

    private void setHistoryToRedis(Message<JsonObject> message) {
        redisAPI.hincrby(Key.REDIS_CLICKS_HISTORY_HASH, LocalDate.now().toString(), "1", res -> {
            if (res.failed()) {
                log.error("setHistoryToRedis Fail to get data from Redis", res.cause());
            }
        });
        redisAPI.hincrby(Key.REDIS_CLICKS_TOTAL_HASH, "total", "1", res -> {
            if (res.failed()) {
                log.error("setHistoryToRedis total Fail to get data from Redis", res.cause());
            }
        });
    }

    private void getHistoryFromRedis(Message<JsonObject> message) {
        Future<Response> total = redisAPI.hget(Key.REDIS_CLICKS_TOTAL_HASH, "total");

        // 7天的历史点击量
        LocalDate localDate = LocalDate.now();
        List<String> keys = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            keys.add(localDate.toString());
            localDate = localDate.minusDays(1);
        }
        keys.add(0, Key.REDIS_CLICKS_HISTORY_HASH);
        //HMGET key field [field ...]
        Future<Response> history = redisAPI.hmget(keys);

        CompositeFuture.all(Arrays.asList(total, history)).onComplete(v -> {
            if (v.succeeded()) {
                JsonObject result = new JsonObject();
                result.put("总点击量", total.result());
                result.put("最近七天点击量", history.result());
                message.reply(result);
            } else {
                log.error("日志获取异常", v.cause());
                message.fail(500, v.cause().getMessage());
            }
        });
    }
}
