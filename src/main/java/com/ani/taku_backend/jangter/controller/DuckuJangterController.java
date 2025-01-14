package com.ani.taku_backend.jangter.controller;

import com.ani.taku_backend.common.annotation.RequireUser;
import com.ani.taku_backend.common.response.CommonResponse;
import com.ani.taku_backend.jangter.model.dto.ProductCreateRequestDTO;
import com.ani.taku_backend.jangter.model.dto.ProductFindDetailResponseDTO;
import com.ani.taku_backend.jangter.model.dto.ProductUpdateRequestDTO;
import com.ani.taku_backend.jangter.service.DuckuJangterService;
import com.ani.taku_backend.user.model.dto.PrincipalUser;
import com.ani.taku_backend.user.model.entity.User;
import com.ani.taku_backend.user.service.BlackUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/jangter")
@RequiredArgsConstructor
public class DuckuJangterController {

    private final DuckuJangterService duckuJangterService;
    private final BlackUserService blackUserService;

    /**
     * 판매글 생성
     */
    @Operation(summary = "판매글 생성 생성", description = "덕후 장터 판매글 생성")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "게시글 생성 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 접근"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 카테고리")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RequireUser
    public CommonResponse<Long> createProduct(@Valid ProductCreateRequestDTO requestDTO,
                                              @Parameter(hidden = true) PrincipalUser principalUser) {

        User user = blackUserService.checkBlackUser(principalUser);         // 블랙유저 검증
        Long productId = duckuJangterService.createProduct(requestDTO, user);

        return CommonResponse.created(productId);
    }

    /**
     * 덕후장터 판매글 상세 조회
     */
    @Operation(summary = "판매글 상세 조회", description = "덕후 장터 판매글 상세 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200",description = "게시글 조회 성공"),
            @ApiResponse(responseCode = "403", description = "존재하지 않는 게시글")
    })
    @GetMapping("/{productId}")
    public CommonResponse<ProductFindDetailResponseDTO> findProductDetail(
            @Parameter(description = "게시글 ID", required = true, example = "41") @PathVariable("productId") long productId) {

        log.debug("판매글 컨트롤러 호출");
        ProductFindDetailResponseDTO productDetail = duckuJangterService.findProductDetail(productId, false);

        return CommonResponse.ok(productDetail);
    }

    /**
     * 덕후장터 판매글 업데이트
     */
    @Operation(summary = "판매글 수정", description = "덕후 장터 판매글 수정, 기존 이미지를 삭제하거나 추가 할수 있음")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "게시글 수정 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 접근"),
            @ApiResponse(responseCode = "403", description = "존재하지 않는 게시글"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 카테고리")
    })
    @PutMapping(path = "/{productId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RequireUser
    public CommonResponse<Long> updateProduct(
                        @Parameter(description = "게시글 ID(구글 토큰 입력)", required = true, example = "74")
                        @PathVariable("productId") long productId, @Valid ProductUpdateRequestDTO requestDTO,
                        @Parameter(hidden = true) PrincipalUser principalUser) {

        User user = blackUserService.checkBlackUser(principalUser);        // 블랙 유저인지 검증
        Long updateProductId = duckuJangterService.updateProduct(productId, requestDTO, user);

        return CommonResponse.ok(updateProductId);
    }

    /**
     * 덕후장터 판매글 삭제
     */
    @Operation(
            summary = "판매글 삭제",
            description = "덕후 장터 판매글 삭제")
    @ApiResponses({
            @ApiResponse(responseCode = "200",description = "게시글 삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 접근"),
            @ApiResponse(responseCode = "403", description = "존재하지 않는 게시글"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 카테고리")
    })
    @DeleteMapping("/{productId}")
    public CommonResponse<Void> deleteProduct(
            @Parameter(description = "게시글 ID", required = true) @PathVariable("productId") long productId,
            @Parameter(description = "카테고리 ID", required = true, example = "4") @RequestParam("categoryId") Long categoryId,
            @Parameter(hidden = true) PrincipalUser principalUser) {

        User user = blackUserService.checkBlackUser(principalUser);
        duckuJangterService.deleteProduct(productId, categoryId, user);

        return CommonResponse.ok(null);
    }
}
