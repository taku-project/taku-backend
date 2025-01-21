package com.ani.taku_backend.jangter.service;

import com.ani.taku_backend.jangter.model.dto.ProductCreateRequestDTO;
import com.ani.taku_backend.jangter.model.dto.ProductFindDetailResponseDTO;
import com.ani.taku_backend.jangter.model.dto.ProductUpdateRequestDTO;
import com.ani.taku_backend.user.model.entity.User;

public interface DuckuJangterService {
    Long createProduct(ProductCreateRequestDTO productCreateRequestDTO, User user);

    ProductFindDetailResponseDTO findProductDetail(long productId, boolean isFirstView);

    Long updateProduct(Long productId, ProductUpdateRequestDTO productUpdateRequestDTO, User user);

    void deleteProduct(long productId, User user);
}
