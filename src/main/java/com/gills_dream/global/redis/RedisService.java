package com.gills_dream.global.redis;

import com.gills_dream.global.apiPayload.code.status.ErrorStatus;
import com.gills_dream.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;

    public void setValue(String key, String value, Long timeout) {
        ValueOperations<String, String> values = redisTemplate.opsForValue();
        values.set(key, value, Duration.ofMillis(timeout));
    }

    public String getValue(String key) {
        ValueOperations<String, String> values = redisTemplate.opsForValue();
        String value = values.get(key);
        if (value == null) {
            throw new GeneralException(ErrorStatus.REDIS_KEY_NOT_FOUND);
        }
        return value;
    }

    public void deleteValue(String key) {
        redisTemplate.delete(key);
    }

    public boolean checkExistsValue(String key) {
        return redisTemplate.hasKey(key);
    }
}
