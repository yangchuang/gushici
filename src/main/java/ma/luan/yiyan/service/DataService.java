package ma.luan.yiyan.service;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.buffer.impl.BufferImpl;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.eventbus.ReplyFailure;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisAPI;
import io.vertx.redis.client.Response;
import lombok.extern.slf4j.Slf4j;
import ma.luan.yiyan.constants.Key;
import ma.luan.yiyan.util.CategoryTrie;
import ma.luan.yiyan.util.JsonCollector;
import ma.luan.yiyan.util.OptionsUtil;

import java.util.*;
import java.util.stream.Collectors;


@Slf4j
public class DataService extends AbstractVerticle {
    private RedisAPI redisAPI;
    private Random random = new Random();
    private CategoryTrie keysInRedis = new CategoryTrie();

    @Override
    public void start(Promise<Void> startPromise) {
        vertx.eventBus().consumer(Key.GET_GUSHICI_FROM_REDIS, this::getGushiciFromRedis);
        vertx.eventBus().consumer(Key.GET_HELP_FROM_REDIS, this::getHelpFromRedis);
        Redis redis = Redis.createClient(vertx, OptionsUtil.getRedisOptions(config()));
        redisAPI = RedisAPI.api(redis);

        // 从 redis 缓存所有 key
        Future<Response> imgKeys = redisAPI.keys(Key.IMG);
        Future<Response>  jsonKeys = redisAPI.keys(Key.IMG);

        CompositeFuture.all(Arrays.asList(imgKeys, jsonKeys)).onComplete(v -> {
            if (v.succeeded()) {
                Set<String> keys = imgKeys.result().getKeys();
                keys.addAll(jsonKeys.result().getKeys());
                keys.stream()
                    .forEach(key -> keysInRedis.insert(key));
                startPromise.complete();
            } else {
                log.error("DataService fail to start", v.cause());
                startPromise.fail(v.cause());
            }
        });
    }

    private void getHelpFromRedis(Message message) {
        redisAPI.lrange(Key.REDIS_HELP_LIST, "0", "-1", res -> {
            if (res.succeeded()) {
                log.info("getHelpFromRedis=============sdewew");
                log.info(res.result().toString());
                Object[] array = res.result().stream().toArray();
                JsonArray newArray = Arrays.stream(array)
                    .map(jsonObject -> {
                        String prefix = config().getString("api.url", "http://localhost/");
                        return new JsonObject(jsonObject.toString()).stream()
                            .collect(Collectors.toMap(Map.Entry::getKey,
                                v -> prefix + v.getValue().toString().replace(":", "/")));
                    })
                    .collect(JsonCollector.toJsonArray());
                System.out.println("================new Array");
                System.out.println(newArray);
                message.reply(newArray);
            } else {
                log.error("getHelpFromRedis Fail to get data from Redis", res.cause());
                message.fail(500, res.cause().getMessage());
            }
        });
    }

    /**
     * @param message example: {format: "png", categories: [shenghuo, buyi]}
     */
    private void getGushiciFromRedis(Message<JsonObject> message) {
        JsonArray realCategory = new JsonArray()
            .add("png".equals(message.body().getString("format")) ? "img" : "json")
            .addAll(message.body().getJsonArray("categories"));
        log.info("=======+++++++++++++===========");
        log.info(realCategory.toString());
        checkAndGetKey(realCategory)
            .compose(key -> redisAPI.srandmember(Arrays.asList(key)) // 从 set 随机返回一个对象
            .onComplete(res -> {
                if (res.succeeded()) {
                    log.info("getGushiciFromRedis:"+res.result());
                    message.reply(res.result());
                } else {
                    if (res.cause() instanceof ReplyException) {
                        ReplyException exception = (ReplyException) res.cause();
                        message.fail(exception.failureCode(), exception.getMessage());
                    } else {
                        message.fail(500, res.cause().getMessage());
                    }
                }
            }));
    }

    /**
     * @param categories 用户请求的类别 [img, shenghuo ,buyi]
     * @return 返回一个随机类别的 key （set)
     */
    private Future<String> checkAndGetKey(JsonArray categories) {
        List<String> toRandom = keysInRedis.getKeys(categories);
        if (toRandom.size() >= 1) {
            String key = toRandom.get(random.nextInt(toRandom.size()));
            log.info("checkAndGetKey:"+key);
            return Future.succeededFuture(key);
        } else {
            return Future.failedFuture(new ReplyException(ReplyFailure.RECIPIENT_FAILURE, 404, "没有结果，请检查API"));
        }
    }
}

