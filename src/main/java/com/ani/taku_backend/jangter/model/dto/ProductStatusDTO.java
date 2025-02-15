package com.ani.taku_backend.jangter.model.dto;

import com.ani.taku_backend.jangter.model.entity.DuckuJangter;
import com.ani.taku_backend.marketprice.model.entity.MarketPriceStats;
import lombok.Getter;

@Getter
public class ProductStatusDTO {
    private final DuckuJangter product;
    private final MarketPriceStats latestStats;

    public ProductStatusDTO(DuckuJangter product, MarketPriceStats latestStats) {
        this.product = product;
        this.latestStats = latestStats;
    }
} 