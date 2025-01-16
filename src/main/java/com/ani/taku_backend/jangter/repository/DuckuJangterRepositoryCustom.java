package com.ani.taku_backend.jangter.repository;

import com.ani.taku_backend.jangter.model.dto.responseDto.ProductFindListResponseDto;
import com.ani.taku_backend.jangter.model.entity.DuckuJangter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DuckuJangterRepositoryCustom{

    List<ProductFindListResponseDto> findFilteredProducts(
            String keyword,
            String category,
            Integer minPrice,
            Integer maxPrice,
            String sort,
            String order,
            Long lastId,
            int limit
    );

}
