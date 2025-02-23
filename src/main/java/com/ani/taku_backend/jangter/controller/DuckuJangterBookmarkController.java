package com.ani.taku_backend.jangter.controller;

import com.ani.taku_backend.common.response.CommonResponse;
import com.ani.taku_backend.jangter.model.dto.BookmarkListResponseDTO;
import com.ani.taku_backend.jangter.service.DuckuJangterBookmarkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "장터 북마크 API", description = "장터 상품 북마크 관련 API")
@Slf4j
@RestController
@RequestMapping("/api/bookmarks/jangter")
@RequiredArgsConstructor
public class DuckuJangterBookmarkController {

    private final DuckuJangterBookmarkService bookmarkService;

    @Operation(summary = "장터 상품 북마크 추가", description = "특정 장터 상품을 북마크에 추가합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "북마크 추가 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 상품"),
            @ApiResponse(responseCode = "409", description = "이미 북마크된 상품")
    })
    @PostMapping("/{productId}")
    public CommonResponse<Void> addBookmark(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "북마크할 상품 ID", required = true)
            @PathVariable Long productId
    ) {
        bookmarkService.addBookmark(userId, productId);
        return CommonResponse.ok(null);
    }

    @Operation(summary = "장터 상품 북마크 삭제", description = "특정 장터 상품을 북마크에서 제거합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "북마크 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 북마크")
    })
    @DeleteMapping("/{productId}")
    public CommonResponse<Void> removeBookmark(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "북마크 해제할 상품 ID", required = true)
            @PathVariable Long productId
    ) {
        bookmarkService.removeBookmark(userId, productId);
        return CommonResponse.ok(null);
    }

    @Operation(summary = "장터 북마크 목록 조회", description = "사용자의 장터 북마크 목록을 페이징하여 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "북마크 목록 조회 성공")
    })
    @GetMapping
    public CommonResponse<Page<BookmarkListResponseDTO>> getBookmarkList(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "카테고리 ID (선택사항)")
            @RequestParam(required = false) Long categoryId,
            @ParameterObject
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<BookmarkListResponseDTO> bookmarks = bookmarkService.getBookmarkList(userId, categoryId, pageable);
        return CommonResponse.ok(bookmarks);
    }

    @Operation(summary = "카테고리별 장터 북마크 목록 조회", description = "특정 카테고리의 장터 북마크 목록을 모두 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "카테고리별 북마크 목록 조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 카테고리")
    })
    @GetMapping("/categories/{categoryId}")
    public CommonResponse<List<BookmarkListResponseDTO>> getBookmarkListByCategory(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "카테고리 ID", required = true)
            @PathVariable Long categoryId
    ) {
        List<BookmarkListResponseDTO> bookmarks = bookmarkService.getBookmarkListByCategory(userId, categoryId);
        return CommonResponse.ok(bookmarks);
    }
}