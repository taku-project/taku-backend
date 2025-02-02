package com.ani.taku_backend.jangter.service;

import com.ani.taku_backend.jangter.model.dto.ProductCreateRequestDTO;
import com.ani.taku_backend.jangter.model.dto.ProductFindDetailResponseDTO;
import com.ani.taku_backend.jangter.model.dto.ProductRankInfoResponseDTO;
import com.ani.taku_backend.jangter.model.dto.ProductRecommendResponseDTO;
import com.ani.taku_backend.jangter.model.dto.ProductUpdateRequestDTO;
import com.ani.taku_backend.user.model.dto.PrincipalUser;
import com.ani.taku_backend.user.model.entity.User;
import org.springframework.web.bind.annotation.PathVariable;

public interface DuckuJangterService {
    Long createProduct(ProductCreateRequestDTO productCreateRequestDTO, User user);

    ProductFindDetailResponseDTO findProductDetail(long productId, boolean isFirstView);

    Long updateProduct(Long productId, ProductUpdateRequestDTO productUpdateRequestDTO, User user);

    void deleteProduct(long productId, User user);

    ProductRecommendResponseDTO recommendProduct(Long productId, PrincipalUser principalUser);

    ProductRankInfoResponseDTO getJangterRank();
}
