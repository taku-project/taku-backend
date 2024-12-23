package com.ani.taku_backend.common.service;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void setKeyValue(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public void setKeyValue(String key, String value, Duration validityTime) {
        redisTemplate.opsForValue().set(key, value, validityTime);
    }

    public String getKeyValue(String key) {
        return (String) redisTemplate.opsForValue().get(key);
    }

    public void deleteKeyValue(String key) {
        redisTemplate.delete(key);
    }

    public List<String> getKeyValues(String key) throws Exception {
        return (List<String>) redisTemplate.opsForValue().get(key);
    }

    public void setKeyValues(String key, List<String> values) {
        redisTemplate.opsForValue().set(key, values);
    }

    public void setKeyValues(String key, List<String> values, Duration validityTime) {
        redisTemplate.opsForValue().set(key, values, validityTime);
    }
}
