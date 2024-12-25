package com.ani.taku_backend.common.service;

import java.time.Duration;

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
}
