package ma.luan.yiyan.util;

import io.vertx.core.json.JsonObject;
import io.vertx.redis.client.RedisOptions;

import java.util.Arrays;

public class OptionsUtil {
    public static RedisOptions getRedisOptions(JsonObject config) {
        return new RedisOptions(config);
    }
}
