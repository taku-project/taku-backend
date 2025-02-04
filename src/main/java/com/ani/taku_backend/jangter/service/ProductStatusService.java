package com.ani.taku_backend.jangter.service;

import com.ani.taku_backend.common.enums.ProductStatusType;
import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.jangter.model.entity.DuckuJangter;
import com.ani.taku_backend.jangter.repository.DuckuJangterRepository;
import com.ani.taku_backend.user.model.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.ani.taku_backend.common.exception.ErrorCode.FORBIDDEN_ACCESS;
import static com.ani.taku_backend.common.exception.ErrorCode.NOT_FOUND_POST;

/**
 * 상품 상태 관리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductStatusService {
    private final DuckuJangterRepository duckuJangterRepository;

    /**
     * 상품 상태 업데이트
     * @param productId 상품 ID
     * @param status 변경할 상태
     * @param user 요청한 사용자
     */
    @Transactional
    public void updateProductStatus(Long productId, ProductStatusType status, User user) {
        DuckuJangter product = duckuJangterRepository.findById(productId)
                .orElseThrow(() -> new DuckwhoException(NOT_FOUND_POST));
            
        // 권한 체크 - 상품 소유자만 상태 변경 가능
        if (!product.getUser().getUserId().equals(user.getUserId())) {
            throw new DuckwhoException(FORBIDDEN_ACCESS);
        }
        
        product.updateStatus(status);
        log.info("상품 상태 변경 완료 - productId: {}, status: {}", productId, status);
    }
} 