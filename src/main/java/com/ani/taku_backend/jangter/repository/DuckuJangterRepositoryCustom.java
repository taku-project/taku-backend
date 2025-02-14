package com.ani.taku_backend.jangter.repository;


import com.ani.taku_backend.jangter.model.dto.CategoryGroupCountDTO;
import com.ani.taku_backend.jangter.model.dto.ProductViewAndBookmarkDTO;
import com.ani.taku_backend.jangter.model.dto.requestDto.FindRecommendFilteredProductsRequestDTO;
import com.ani.taku_backend.jangter.model.dto.requestDto.ProductFindListRequestDTO;

import com.ani.taku_backend.jangter.model.dto.responseDto.ProductFindListResponseDTO;
import com.ani.taku_backend.jangter.model.entity.DuckuJangter;

import java.util.List;

public interface DuckuJangterRepositoryCustom{

    List<ProductFindListResponseDTO> findFilteredProducts(
            ProductFindListRequestDTO request
    );

    List<DuckuJangter> findRecommendFilteredProducts(
            FindRecommendFilteredProductsRequestDTO request
    );

    List<CategoryGroupCountDTO> findCategoryGroupCount();


    List<ProductViewAndBookmarkDTO> findProductViewAndBookmark(Long categoryId);

    List<ProductViewAndBookmarkDTO> findProductViewAndBookmarkByProductId(Long productId);

}
