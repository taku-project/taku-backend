package com.ani.taku_backend.jangter.controller;

import com.ani.taku_backend.common.annotation.RequireUser;
import com.ani.taku_backend.common.response.CommonResponse;
import com.ani.taku_backend.jangter.model.dto.ProductCreateRequestDTO;
import com.ani.taku_backend.jangter.model.dto.ProductFindDetailResponseDTO;
import com.ani.taku_backend.jangter.model.dto.ProductUpdateRequestDTO;
import com.ani.taku_backend.jangter.service.DuckuJangterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/jangter")
@RequiredArgsConstructor
public class DuckuJangterController {

    private final DuckuJangterService duckuJangterService;

    /**
     * 판매글 생성
     */
    @Operation(
            summary = "판매글 생성 생성",
            description = """
                    덕후 장터 판매글 생성
                    - createDTO: 판매글 정보를 포함한 JSON 데이터
                    - productImage: 첨부할 이미지 파일 리스트 (이미지 파일, 필수값 아님)
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "게시글 생성 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 접근"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 카테고리")
    })
    @PostMapping(consumes = "multipart/form-data")
    public CommonResponse<Long> createProduct(
                @Parameter(
                        description = "판매글 생성 요청 JSON 데이터", required = true
                )
                @RequestPart("createDTO") @Valid ProductCreateRequestDTO requestDTO,
                @Parameter(
                        description = "판매글 첨부 이미지(필수값 아님)"
                )
                @RequestPart(value = "productImage", required = false) List<MultipartFile> imageList) {

        Long productId = duckuJangterService.createProduct(requestDTO, imageList, null);

        return CommonResponse.created(productId);
    }

    /**
     * 덕후장터 판매글 상세 조회
     */
    @Operation(
            summary = "판매글 상세 조회",
            description = "덕후 장터 판매글 상세 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200",description = "게시글 수정 성공"),
            @ApiResponse(responseCode = "403", description = "존재하지 않는 게시글")
    })
    @GetMapping("/{productId}")
    public CommonResponse<ProductFindDetailResponseDTO> findProductDetail(
            @Parameter(description = "게시글 ID", required = true) @PathVariable("productId") long productId) {
        log.info("판매글 컨트롤러 호출");
        ProductFindDetailResponseDTO productDetail = duckuJangterService.findProductDetail(productId);
        return CommonResponse.ok(productDetail);
    }

    /**
     * 덕후장터 판매글 업데이트
     */
    @Operation(
            summary = "판매글 수정",
            description = """
                    덕후 장터 판매글 수정
                    - updateDTO: 판매글 정보를 포함한 JSON 데이터
                    - updateImage: 첨부할 이미지 파일 리스트 (이미지 파일, 필수값 아님)
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200",description = "게시글 수정 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 접근"),
            @ApiResponse(responseCode = "403", description = "존재하지 않는 게시글"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 카테고리")
    })
    @PutMapping("/{productId}")
    @RequireUser
    public CommonResponse<Long> updateProduct(
                        @Parameter(description = "게시글 ID", required = true) @PathVariable("productId") long productId,
                        @Parameter(
                              description = "판매글 생성 요청 JSON 데이터", required = true
                        )
                        @RequestPart("updateDTO") ProductUpdateRequestDTO requestDTO,
                        @Parameter(
                              description = "새로 업로드한 이미지"
                        )
                        @RequestPart(value = "updateImage", required = false) List<MultipartFile> imageList) {

        Long updateProductId = duckuJangterService.updateProduct(productId, requestDTO, imageList, null);

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
    @RequireUser
    public void deleteProduct(
            @Parameter(description = "게시글 ID", required = true) @PathVariable("productId") long productId,
            @Parameter(description = "카테고리 ID", required = true) @RequestParam("categoryId") Long categoryId) {
        duckuJangterService.deleteProduct(productId, categoryId, null);
    }
}
