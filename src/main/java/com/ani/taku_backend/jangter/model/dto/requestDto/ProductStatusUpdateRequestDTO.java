package com.ani.taku_backend.jangter.model.dto.requestDto;

import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.exception.ErrorCode;
import com.ani.taku_backend.jangter.model.enums.ProductStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProductStatusUpdateRequestDTO {
    
    @NotNull(message = "상태는 필수 입력값입니다.")
    private ProductStatus status;
    
    @Positive(message = "판매가는 0보다 커야 합니다.")
    private Long soldPrice;
    
    public ProductStatusUpdateRequestDTO(ProductStatus status, Long soldPrice) {
        this.status = status;
        this.soldPrice = soldPrice;
        validateSoldPrice();
    }

    private void validateSoldPrice() {
        if (status == ProductStatus.SOLD_OUT && soldPrice == null) {
            throw new DuckwhoException(ErrorCode.MISSING_SOLD_PRICE);
        }
    }
} 