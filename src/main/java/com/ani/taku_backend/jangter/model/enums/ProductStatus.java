package com.ani.taku_backend.jangter.model.enums;

import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductStatus {
    FOR_SALE("판매중"),
    RESERVED("예약중"),
    SOLD_OUT("거래완료");

    private final String description;

    public void validateTransitionTo(ProductStatus newStatus) {
        if (this == newStatus) {
            throw new DuckwhoException(ErrorCode.INVALID_STATUS_TRANSITION);
        }

        boolean isValid = switch (this) {
            case FOR_SALE -> newStatus == RESERVED || newStatus == SOLD_OUT;
            case RESERVED -> newStatus == FOR_SALE || newStatus == SOLD_OUT;
            case SOLD_OUT -> false;
        };

        if (!isValid) {
            throw new DuckwhoException(ErrorCode.INVALID_STATUS_TRANSITION);
        }
    }
} 
