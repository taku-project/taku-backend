package com.ani.taku_backend.jangter.controller;

import com.ani.taku_backend.common.annotation.RequireUser;
import com.ani.taku_backend.common.enums.LogType;
import com.ani.taku_backend.common.enums.SortFilterType;
import com.ani.taku_backend.common.response.CommonResponse;
import com.ani.taku_backend.jangter.model.dto.ProductCreateRequestDTO;
import com.ani.taku_backend.jangter.model.dto.ProductFindDetailResponseDTO;
import com.ani.taku_backend.jangter.model.dto.ProductRankInfoResponseDTO;
import com.ani.taku_backend.jangter.model.dto.ProductRecommendResponseDTO;
import com.ani.taku_backend.jangter.model.dto.ProductUpdateRequestDTO;
import com.ani.taku_backend.jangter.model.dto.requestDto.ProductStatusUpdateRequestDTO;

import com.ani.taku_backend.jangter.model.dto.responseDto.ProductFindListResponseDTO;
import com.ani.taku_backend.jangter.model.dto.requestDto.ProductFindListRequestDTO;
import com.ani.taku_backend.jangter.model.entity.UserInteraction;
import com.ani.taku_backend.jangter.model.entity.UserInteraction.SearchLogDetail;

import com.ani.taku_backend.jangter.service.DuckuJangterService;
import com.ani.taku_backend.jangter.service.UserInteractionService;
import com.ani.taku_backend.user.model.dto.PrincipalUser;
import com.ani.taku_backend.user.model.entity.User;
import com.ani.taku_backend.user.service.BlackUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/jangter")
@RequiredArgsConstructor
public class DuckuJangterController {

    private final DuckuJangterService duckuJangterService;
    private final UserInteractionService userInteractionService;
    private final BlackUserService blackUserService;

    /**
     * 판매글 생성
     */
    @Operation(summary = "판매글 생성 생성", description = """
                        덕후 장터 판매글 생성\n
                        imageList - 추가할 이미지 리스트(이미지 파일)
                        """)
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "게시글 생성 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 접근"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 카테고리")
    })
    @RequireUser
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CommonResponse<Long> createProduct(
                                @Valid ProductCreateRequestDTO requestDTO,
                                @Parameter(hidden = true) PrincipalUser principalUser) {

        User user = blackUserService.checkBlackUser(principalUser);         // 블랙유저 검증
        Long productId = duckuJangterService.createProduct(requestDTO, user);

        return CommonResponse.created(productId);
    }

    /**
     * 덕후 장터 판매 글 전체 목록 조회
     */

    @Operation(summary = "판매글 전체 조회", description = "덕후 장터 판매글 전체 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200",description = "성공"),

    })
    @GetMapping("/products")
    public CommonResponse<List<ProductFindListResponseDTO>> findProductItems(@ModelAttribute ProductFindListRequestDTO request){

        SortFilterType sortFilterType;

        boolean isDesc = "desc".equalsIgnoreCase(request.getOrder());
        boolean isDaySort = "day".equalsIgnoreCase(request.getSort());

        if (isDesc) {
            sortFilterType = isDaySort ? SortFilterType.OLDEST : SortFilterType.PRICE_DESC;
        } else {
            sortFilterType =  isDaySort ? SortFilterType.LATEST : SortFilterType.PRICE_ASC;
        }

        UserInteraction.LogDetail logDetail = SearchLogDetail.builder()
                .searchKeyword(request.getSearchKeyword())
                .searchCategory(Collections.singletonList(request.getCategoryId()))
                .sortType(sortFilterType)
                .build();


        userInteractionService.saveLog(null, LogType.SEARCH, logDetail );

        List<ProductFindListResponseDTO> products = duckuJangterService.getProducts(request);

        return CommonResponse.ok(products);
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
            @Parameter(description = "판매글 ID", required = true, example = "20") @PathVariable("productId") long productId) {

        log.debug("판매글 컨트롤러 호출");
        ProductFindDetailResponseDTO productDetail = duckuJangterService.findProductDetail(productId, false);

        return CommonResponse.ok(productDetail);
    }

    /**
     * 덕후장터 판매글 업데이트
     */
    @Operation(summary = "판매글 수정",
            description = """
                        덕후 장터 판매글 수정, 기존 이미지를 삭제하거나 추가 할수 있음(스웨거 오류로 여기다 설명)\n
                        deleteImageUrl - 기존 글에서 삭제된 이미지 Url 리스트(문자열)\n
                        imageList - 추가된 이미지 리스트(이미지 파일)
                        """)
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
                        @PathVariable("productId") long productId,
                        @Valid ProductUpdateRequestDTO requestDTO,
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
            @Parameter(hidden = true) PrincipalUser principalUser) {

        User user = blackUserService.checkBlackUser(principalUser);
        duckuJangterService.deleteProduct(productId, user);

        return CommonResponse.ok(null);
    }


    @Operation(
            summary = "판매글 추천",
            description = "판매글 추천 API (로그인/비로그인 모두 가능)",
            security = { @SecurityRequirement(name = "Bearer Auth") }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",description = "게시글 추천"),
    })
    @GetMapping("/{productId}/recommend")
    public CommonResponse<ProductRecommendResponseDTO> recommendProduct(@PathVariable("productId") Long productId) {
        ProductRecommendResponseDTO recommendProduct = this.duckuJangterService.recommendProduct(productId, null);
        return CommonResponse.ok(recommendProduct);
    }


    @Operation(summary = "장터 랭킹 일괄 조회", description = "장터 랭킹 일괄 조회")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "장터 랭킹 조회 성공")
    })
    @GetMapping("/rank")
    public CommonResponse<ProductRankInfoResponseDTO> getJangterRank() {
        ProductRankInfoResponseDTO productRankInfoResponseDTO = this.duckuJangterService.getJangterRank();
        return CommonResponse.ok(productRankInfoResponseDTO);
    }

    @Operation(summary = "상품 상태 변경", 
            description = "상품의 상태를 변경합니다 (판매중 -> 예약중 -> 판매완료)",
            security = { @SecurityRequirement(name = "Bearer Auth") })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상태 변경 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 상태 변경 요청"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 접근"),
            @ApiResponse(responseCode = "403", description = "상태 변경 권한 없음"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 상품")
    })
    @PatchMapping("/{productId}/status")
    @RequireUser
    public CommonResponse<Void> updateProductStatus(
            @Parameter(description = "상품 ID", required = true) 
            @PathVariable("productId") Long productId,
            @Valid @RequestBody ProductStatusUpdateRequestDTO requestDTO,
            @Parameter(hidden = true) PrincipalUser principalUser) {
            
        User user = blackUserService.checkBlackUser(principalUser);
        duckuJangterService.updateProductStatus(productId, requestDTO, user);
        return CommonResponse.ok(null);
    }
}
