package com.ani.taku_backend.jangter.controller;

import com.ani.taku_backend.common.annotation.RequireUser;
import com.ani.taku_backend.common.response.ApiResponse;
import com.ani.taku_backend.jangter.model.dto.ProductCreateRequestDTO;
import com.ani.taku_backend.jangter.model.dto.ProductFindDetailResponseDTO;
import com.ani.taku_backend.jangter.model.dto.ProductUpdateRequestDTO;
import com.ani.taku_backend.jangter.service.DuckuJangterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/jangter")
@RequiredArgsConstructor
public class DuckuJangterController {

    private final DuckuJangterService duckuJangterService;

    @PostMapping
    public ApiResponse<Long> createProduct(
            @RequestPart("createPost") ProductCreateRequestDTO requestDTO,
            @RequestPart(value = "productImage", required = false) List<MultipartFile> imageList) {

        Long productId = duckuJangterService.createProduct(requestDTO, imageList, null);

        return ApiResponse.created(productId);
    }

    @GetMapping("/{productId}")
    public ApiResponse<ProductFindDetailResponseDTO> findProductDetail(@PathVariable long productId) {
        ProductFindDetailResponseDTO productDetail = duckuJangterService.findProductDetail(productId);
        return ApiResponse.ok(productDetail);
    }

    @PutMapping("/{productId}")
    @RequireUser
    public ApiResponse<Long> updateProduct(@PathVariable long productId,
                @RequestPart("createPost") ProductUpdateRequestDTO requestDTO,
                @RequestPart(value = "productImage", required = false) List<MultipartFile> imageList) {

        Long updateProductId = duckuJangterService.updateProduct(productId, requestDTO, imageList, null);

        return ApiResponse.ok(updateProductId);
    }

    @DeleteMapping("/{productId}")
    @RequireUser
    public void deleteProduct(@PathVariable long productId) {

    }
}
