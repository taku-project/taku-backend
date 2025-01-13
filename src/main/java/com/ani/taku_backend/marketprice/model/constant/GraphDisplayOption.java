package com.ani.taku_backend.marketprice.model.constant;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "그래프 표시 옵션")
public enum GraphDisplayOption {
    @Schema(description = "등록가만 표시")
    REGISTERED_PRICE_ONLY,

    @Schema(description = "판매가만 표시")
    SOLD_PRICE_ONLY,

    @Schema(description = "모두 표시")
    ALL;
}