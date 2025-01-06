package com.ani.taku_backend.jangter.service.viewcount;

import com.ani.taku_backend.common.service.RedisService;
import com.ani.taku_backend.common.util.RedisKeyUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * 해당 코드는 실제 애플리케이션에 반영되지 않습니다, 회고용으로 남겨두었습니다
 * 진호님이 만들어주신 조회수 증가 AOP 적용 예정입니다.
 */
@Slf4j
//@Service
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
