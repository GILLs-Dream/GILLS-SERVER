package com.gills_dream.global.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gills_dream.domain.token.dto.TokenClass;
import com.gills_dream.global.apiPayload.code.status.ErrorStatus;
import com.gills_dream.global.apiPayload.exception.GeneralException;
import com.gills_dream.global.common.record.TokenPair;
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
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void setValue(String key, String value, Long timeout) {
        ValueOperations<String, String> values = redisTemplate.opsForValue();
        values.set(key, value, Duration.ofMillis(timeout));
    }

    public void setValue(String tempCode, TokenClass tokenClass, Long timeout) {
        try {
            ValueOperations<String, String> values = redisTemplate.opsForValue();
            values.set(tempCode, objectMapper.writeValueAsString(tokenClass), Duration.ofMillis((timeout)));
        } catch (JsonProcessingException e) {
            throw new GeneralException(ErrorStatus.CANNOT_SERIALIZE_VALUE);
        }
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
