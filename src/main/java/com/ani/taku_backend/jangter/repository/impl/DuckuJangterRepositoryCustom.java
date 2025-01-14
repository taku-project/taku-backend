package com.ani.taku_backend.jangter.repository.impl;

import java.math.BigDecimal;
import java.util.List;

import com.ani.taku_backend.common.enums.StatusType;
import com.ani.taku_backend.jangter.model.dto.CategoryGroupCountDTO;
import com.ani.taku_backend.jangter.model.entity.DuckuJangter;

public interface DuckuJangterRepositoryCustom {

    List<DuckuJangter> findRecommendFilteredProducts(
        List<String> keywords, 
        BigDecimal minPrice, 
        BigDecimal maxPrice, 
        Long itemCategoryId,
        StatusType status,
        Long productId
    );

    List<CategoryGroupCountDTO> findCategoryGroupCount();

    
}