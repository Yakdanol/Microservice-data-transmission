package ru.mai.lessons.rpks.impl;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.JedisPooled;
import ru.mai.lessons.rpks.RedisClient;
import ru.mai.lessons.rpks.model.Message;

import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

@Slf4j
public class RedisClientImpl implements RedisClient {
    JedisPooled jedisPooled;

    public RedisClientImpl(Config config) {
        Config redisConfig = config.getConfig("redis");
        Properties redisConfigProperties = new Properties();
        for (Map.Entry<String, ConfigValue> entry : redisConfig.entrySet()) {
            redisConfigProperties.setProperty(entry.getKey(), redisConfig.getString(entry.getKey()));
        }

        this.jedisPooled = new JedisPooled(redisConfigProperties.getProperty("host"), Integer.parseInt(redisConfigProperties.getProperty("port")));
    }

    @Override
    public void processing(String key, Message message, Long time) {
        try {
            if (jedisPooled.exists(key)) {
                message.setDeduplicationState(false);
            } else {
                message.setDeduplicationState(true);
                jedisPooled.set(key, message.getValue());
                jedisPooled.expire(key, time);
            }

        } catch (Exception ex) {
            log.error(ex.getMessage());
            log.error(Arrays.deepToString(ex.getStackTrace()));
        }
    }

    @Override
    public void close() {
        jedisPooled.close();
    }
}
