package com.ani.taku_backend.common.service;

import java.io.Closeable;
import java.time.Duration;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;

@Slf4j
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

    /**
     * 조회수 관련 로직 추가
     */
    // key의 값을 1 증가 -> 조회수 증가
    public void increment(String key) {
        redisTemplate.opsForValue().increment(key);
    }

    // 특정 key의 값을 Long 타입으로 반환 -> 조회수 get
    public Long getViewCount(String key) {
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? Long.parseLong(value.toString()) : null;
    }

    /**
     * 특정 패턴에 맞는 모든 키값을 가져옴 (데이터 부하를 줄이기 위해 SCAN 명령어를 사용..)
     * keys로 가져오면 모든 키를 한번에 받아오기 때문에 데이터 셋이 클 경우 Redis 서버에 부하를 줄 수 있다고 함(운영 서버에는 권장되지 않는다고함)
     * 그러나 사실 작은 서버에서는 keys(pattern)으로 간단히 구현할 수 있다고 함
     * SCAN 명령어를 사용하면 키를 점진적으로 검색하여 반환하여 서버의 부하를 줄여 대규모 데이터셋에서 권장된다고 함
     */
    public Set<String> getKeysByPattern(String pattern) {

        // keys방식
//        return redisTemplate.keys(pattern);

        Set<String> keys = new HashSet<>();

        ScanOptions scanOptions = ScanOptions.scanOptions().match(pattern).count(1000).build();

        try (Cursor<byte[]> cursor = redisTemplate.executeWithStickyConnection(
                (RedisCallback<Cursor<byte[]>>) connection -> connection.scan(scanOptions))) {
            while (cursor.hasNext()) {
                String key = new String(cursor.next());
                keys.add(key);
            }
        } catch (Exception e) {
            log.error("패턴 스캔 오류: {}", e.getMessage());
        }
        return keys;
    }
}
