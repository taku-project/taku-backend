package com.ani.taku_backend.jangter.model.dto.requestDto;

import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.exception.ErrorCode;
import com.ani.taku_backend.jangter.model.enums.ProductStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "상품 상태 변경 요청 DTO")
public class ProductStatusUpdateRequestDTO {
    
    @NotNull(message = "상태는 필수 입력값입니다.")
    @Schema(description = "변경할 상품 상태", example = "RESERVED 또는 SOLD_OUT")
    private ProductStatus status;
    
    @Schema(description = "판매 완료 가격 (SOLD_OUT 상태로 변경 시 필수)", example = "50000")
    private Long soldPrice;
    
    public ProductStatusUpdateRequestDTO(ProductStatus status, Long soldPrice) {
        this.status = status;
        this.soldPrice = soldPrice;
    }

    public void validateSoldPrice() {
        if (status == ProductStatus.SOLD_OUT) {
            if (soldPrice == null) {
                throw new DuckwhoException(ErrorCode.MISSING_SOLD_PRICE);
            }
            if (soldPrice < 0) {
                throw new DuckwhoException(ErrorCode.INVALID_INPUT_VALUE);
            }
        }
    }
} 
