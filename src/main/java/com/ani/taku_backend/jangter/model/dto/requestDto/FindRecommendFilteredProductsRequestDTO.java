package com.ani.taku_backend.jangter.model.dto.requestDto;

import com.ani.taku_backend.jangter.model.enums.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class FindRecommendFilteredProductsRequestDTO {

    private List<String> keywords;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Long itemCategoryId;
    private ProductStatus status;
    private Long productId;
}
