package com.ani.taku_backend.jangter.repository;

import com.ani.taku_backend.common.enums.StatusType;
import com.ani.taku_backend.jangter.model.dto.requestDto.FindRecommendFilteredProductsRequestDTO;
import com.ani.taku_backend.jangter.model.dto.requestDto.ProductFindListRequestDto;
import com.ani.taku_backend.jangter.model.dto.responseDto.ProductFindListResponseDto;
import com.ani.taku_backend.jangter.model.entity.DuckuJangter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;

public interface DuckuJangterRepositoryCustom{

    List<ProductFindListResponseDto> findFilteredProducts(
            ProductFindListRequestDto request
    );

    List<DuckuJangter> findRecommendFilteredProducts(
            FindRecommendFilteredProductsRequestDTO request
    );

}
