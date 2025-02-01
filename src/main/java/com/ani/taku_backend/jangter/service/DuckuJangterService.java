package com.ani.taku_backend.jangter.service;

import com.ani.taku_backend.jangter.model.dto.ProductCreateRequestDTO;
import com.ani.taku_backend.jangter.model.dto.ProductFindDetailResponseDTO;
import com.ani.taku_backend.jangter.model.dto.ProductRankInfoResponseDTO;
import com.ani.taku_backend.jangter.model.dto.ProductRecommendResponseDTO;
import com.ani.taku_backend.jangter.model.dto.ProductUpdateRequestDTO;
import com.ani.taku_backend.jangter.model.dto.requestDto.ProductFindListRequestDto;
import com.ani.taku_backend.jangter.model.dto.responseDto.ProductFindListResponseDto;
import com.ani.taku_backend.jangter.repository.DuckuJangterRepository;
import com.ani.taku_backend.user.model.dto.PrincipalUser;
import com.ani.taku_backend.user.model.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;


public interface DuckuJangterService {

    Long createProduct(ProductCreateRequestDTO productCreateRequestDTO, User user);

    ProductFindDetailResponseDTO findProductDetail(long productId, boolean isFirstView);

    Long updateProduct(Long productId, ProductUpdateRequestDTO productUpdateRequestDTO, User user);

    void deleteProduct(long productId, User user);

    ProductRecommendResponseDTO recommendProduct(Long productId, PrincipalUser principalUser);

    List<ProductFindListResponseDto> getProducts(ProductFindListRequestDto request);

    ProductRankInfoResponseDTO getJangterRank();
}
