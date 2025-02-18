package com.ani.taku_backend.jangter.vo;

import java.math.BigDecimal;
import java.util.List;

import com.ani.taku_backend.jangter.model.entity.DuckuJangter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserBookmarkHistory {

    private List<Long> categoryIds;

    private List<String> keywords;

    private BigDecimal minPrice;

    private BigDecimal maxPrice;

    private BigDecimal avgPrice;

    public static UserBookmarkHistory create(List<DuckuJangter> buyUserProducts , List<String> keywords){
        return UserBookmarkHistory.builder()
        .keywords(keywords)
        .categoryIds(buyUserProducts.stream().map(item -> item.getItemCategories().getId()).distinct().toList())
        .avgPrice(buyUserProducts.stream().map(item -> item.getPrice()).reduce(BigDecimal.ZERO, BigDecimal::add).divide(BigDecimal.valueOf(buyUserProducts.size())))
        .minPrice(buyUserProducts.stream().map(item -> item.getPrice()).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO))
        .maxPrice(buyUserProducts.stream().map(item -> item.getPrice()).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO))
        .build();
    }
}


