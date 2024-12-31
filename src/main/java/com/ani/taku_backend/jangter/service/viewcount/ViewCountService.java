package com.ani.taku_backend.jangter.service.viewcount;

import com.ani.taku_backend.common.service.RedisService;
import com.ani.taku_backend.common.util.RedisKeyUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ViewCountService {

    private final RedisService redisService;

    // 조회 수 증가
    public void incrementViewCount(Long productId) {
        String redisKey = RedisKeyUtil.getViewCountKey(productId);
        redisService.increment(redisKey);
    }

    // 조회 수 가져오기
    public Long getViewCount(Long productId) {
        String redisKey = RedisKeyUtil.getViewCountKey(productId);
        return redisService.getViewCount(redisKey);
    }
}
