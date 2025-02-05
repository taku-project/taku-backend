package com.ani.taku_backend.jangter.model.dto;

import com.ani.taku_backend.jangter.enums.ProductStatusType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 상품 상태 업데이트 요청 DTO
 */
@Getter
@NoArgsConstructor
@Schema(description = "상품 상태 업데이트 요청")
public class ProductStatusUpdateRequestDTO {
    
    @NotNull(message = "상품 상태는 필수입니다")
    @Schema(
        description = "변경할 상품 상태",
        example = "FOR_SALE",
        oneOf = ProductStatusType.class,
        enumAsRef = true
    )
    private ProductStatusType status;
} 