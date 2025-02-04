package com.ani.taku_backend.jangter.model.dto;

import com.ani.taku_backend.common.enums.ProductStatusType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 상품 상태 업데이트 요청 DTO
 */
@Getter
@NoArgsConstructor
public class ProductStatusUpdateRequestDTO {
    @NotNull(message = "상품 상태는 필수입니다")
    private ProductStatusType status;
} 