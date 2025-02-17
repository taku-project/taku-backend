package com.ani.taku_backend.bookmark.controller;

import com.ani.taku_backend.bookmark.domain.dto.DuckuJangterBookmarkResponseDTO;
import com.ani.taku_backend.bookmark.service.DuckuJangterBookmarkService;
import com.ani.taku_backend.common.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "덕후 장터 북마크 API", description = "덕후 장터 상품 북마크 관련 API")
@RestController
@RequestMapping("/api/jangter/bookmarks")
@RequiredArgsConstructor
public class DuckuJangterBookmarkController {

    private final DuckuJangterBookmarkService duckuJangterBookmarkService;

    @Operation(
        summary = "상품 북마크 추가",
        description = "특정 상품을 사용자의 북마크 목록에 추가합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "북마크 추가 성공"),
        @ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음"),
        @ApiResponse(responseCode = "409", description = "이미 북마크된 상품")
    })
    @PostMapping("/{productId}")
    public CommonResponse<Void> addBookmark(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @Parameter(description = "북마크할 상품 ID", example = "1") @PathVariable Long productId) {
        duckuJangterBookmarkService.addBookmark(userId, productId);
        return CommonResponse.ok(null);
    }

    @Operation(
        summary = "상품 북마크 삭제",
        description = "특정 상품을 사용자의 북마크 목록에서 제거합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "북마크 삭제 성공"),
        @ApiResponse(responseCode = "404", description = "북마크를 찾을 수 없음")
    })
    @DeleteMapping("/{productId}")
    public CommonResponse<Void> removeBookmark(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @Parameter(description = "삭제할 북마크 상품 ID", example = "1") @PathVariable Long productId) {
        duckuJangterBookmarkService.removeBookmark(userId, productId);
        return CommonResponse.ok(null);
    }

    @Operation(
        summary = "북마크 목록 조회",
        description = "사용자의 북마크된 상품 목록을 페이지네이션하여 조회합니다."
    )
    @Parameters({
        @Parameter(
            name = "page",
            description = "페이지 번호 (0부터 시작)",
            in = ParameterIn.QUERY,
            schema = @Schema(type = "integer", defaultValue = "0")
        ),
        @Parameter(
            name = "size",
            description = "페이지 크기",
            in = ParameterIn.QUERY,
            schema = @Schema(type = "integer", defaultValue = "20")
        ),
        @Parameter(
            name = "sort",
            description = "정렬 기준 (예: createdAt,desc)",
            in = ParameterIn.QUERY,
            schema = @Schema(type = "string", defaultValue = "createdAt,desc")
        )
    })
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "북마크 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = DuckuJangterBookmarkResponseDTO.class))
        ),
        @ApiResponse(responseCode = "404", description = "북마크를 찾을 수 없음")
    })
    @GetMapping
    public CommonResponse<Page<DuckuJangterBookmarkResponseDTO>> getBookmarks(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return CommonResponse.ok(
                duckuJangterBookmarkService.getBookmarks(userId, pageable));
    }

    @Operation(
        summary = "카테고리별 북마크 목록 조회",
        description = "특정 카테고리의 북마크된 상품 목록을 페이지네이션하여 조회합니다."
    )
    @Parameters({
        @Parameter(
            name = "page",
            description = "페이지 번호 (0부터 시작)",
            in = ParameterIn.QUERY,
            schema = @Schema(type = "integer", defaultValue = "0")
        ),
        @Parameter(
            name = "size",
            description = "페이지 크기",
            in = ParameterIn.QUERY,
            schema = @Schema(type = "integer", defaultValue = "20")
        ),
        @Parameter(
            name = "sort",
            description = "정렬 기준 (예: createdAt,desc)",
            in = ParameterIn.QUERY,
            schema = @Schema(type = "string", defaultValue = "createdAt,desc")
        )
    })
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "카테고리별 북마크 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = DuckuJangterBookmarkResponseDTO.class))
        ),
        @ApiResponse(responseCode = "404", description = "북마크를 찾을 수 없음")
    })
    @GetMapping("/category/{category}")
    public CommonResponse<Page<DuckuJangterBookmarkResponseDTO>> getBookmarksByCategory(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @Parameter(description = "조회할 카테고리명", example = "피규어") @PathVariable String category,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return CommonResponse.ok(
                duckuJangterBookmarkService.getBookmarksByCategory(userId, category, pageable));
    }
} 
