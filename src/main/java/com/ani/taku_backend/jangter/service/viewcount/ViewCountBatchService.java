package com.ani.taku_backend.jangter.service.viewcount;

import com.ani.taku_backend.common.service.RedisService;
import com.ani.taku_backend.common.util.RedisKeyUtil;
import com.ani.taku_backend.jangter.repository.DuckuJangterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ViewCountBatchService {

    private final RedisService redisService;
    private final DuckuJangterRepository duckuJangterRepository;

    @Scheduled(fixedRate = 1800000) // 30분 = 30 * 60 * 1000 ms
    @Transactional
    public void updateViewCount() {
        String patternKey = RedisKeyUtil.getViewCountPatternKey();
        Set<String> keysByPattern = redisService.getKeysByPattern(patternKey);

        for (String key : keysByPattern) {
            Long productId = extractProductIdFromKey(key);
            if (productId == null) {
                continue;   // extractProductIdFromKey에서 null이 넘어오면 건너뜀(추출 실패)
            }
            Long viewCount = redisService.getViewCount(key);
            if (viewCount == null || viewCount <= 0) {
                continue;   // key로 꺼낸 조회수가 없거나음수면 제외
            }

            try {
                duckuJangterRepository.updateViewCount(productId, viewCount);
                redisService.deleteKeyValue(key);
                log.info("조회수 RDB 업데이트, Redis 삭제 {}", key);
            } catch (Exception e) {
                log.error("조회수 업데이트 실패 for key: {}", key, e);
            }
        }
    }

    private Long extractProductIdFromKey(String key) {
        String prefix = "product:viewCount:";
        if (!key.startsWith(prefix)) {
            log.error("키 패턴이 다름: {}", key);
            return null;
        }
        try {
            return Long.parseLong(key.substring(prefix.length()));
        } catch (NumberFormatException e) {
            log.error("키 추출 실패: {}, {}", key, e.getMessage());
            return null;
        }
    }
}
