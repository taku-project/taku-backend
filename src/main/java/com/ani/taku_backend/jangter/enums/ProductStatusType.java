package com.ani.taku_backend.jangter.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 상품 상태를 나타내는 enum
 */
@Schema(description = "상품 상태", enumAsRef = true)
public enum ProductStatusType {
    @Schema(description = "판매중")
    FOR_SALE,        // 판매중
    
    @Schema(description = "예약중")
    RESERVED,        // 예약중
    
    @Schema(description = "판매완료")
    SOLD_OUT,        // 판매완료
} 