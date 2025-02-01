package com.ani.taku_backend.jangter.repository;

import com.ani.taku_backend.common.enums.StatusType;
import com.ani.taku_backend.jangter.model.dto.responseDto.ProductFindListResponseDTO;
import com.ani.taku_backend.jangter.model.entity.DuckuJangter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;

public interface DuckuJangterRepositoryCustom{

    List<ProductFindListResponseDTO> findFilteredProducts(
            String keyword,
            Long categoryId,
            Integer minPrice,
            Integer maxPrice,
            String sort,
            String order,
            Long lastId,
            int limit
    );

    List<DuckuJangter> findRecommendFilteredProducts(
            List<String> keywords,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Long itemCategoryId,
            StatusType status,
            Long productId
    );

}
