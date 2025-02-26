package com.ani.taku_backend.jangter.controller;

import com.ani.taku_backend.common.response.CommonResponse;
import com.ani.taku_backend.jangter.model.dto.BookmarkListResponseDTO;
import com.ani.taku_backend.jangter.service.DuckuJangterBookmarkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.ani.taku_backend.user.model.dto.PrincipalUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.ani.taku_backend.common.dto.CustomPageResponseDTO;

@Tag(name = "장터 북마크 API", description = "장터 상품 북마크 관련 API")
@Slf4j
@RestController
@RequestMapping("/api/bookmarks/jangter")
@RequiredArgsConstructor
public class DuckuJangterBookmarkController {

    private final DuckuJangterBookmarkService bookmarkService;

    @Operation(
        summary = "장터 상품 북마크 추가",
        description = "특정 장터 상품을 북마크에 추가합니다.",
        parameters = {
            @Parameter(name = "productId", description = "북마크할 상품 ID", required = true, in = ParameterIn.PATH)
        }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "북마크 추가 성공"),
            @ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "이미 북마크된 상품")
    })
    @PostMapping
    public CommonResponse<Void> addBookmark(
            @RequestParam("productId") Long productId,
            @AuthenticationPrincipal PrincipalUser principal
    ) {
        bookmarkService.addBookmark(principal.getUser().getUserId(), productId);
        return CommonResponse.ok(null);
    }

    @Operation(
        summary = "장터 상품 북마크 삭제",
        description = "특정 장터 상품을 북마크에서 제거합니다.",
        parameters = {
            @Parameter(name = "productId", description = "북마크 해제할 상품 ID", required = true, in = ParameterIn.PATH)
        }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "북마크 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 북마크")
    })
    @DeleteMapping("/{productId}")
    public CommonResponse<Void> removeBookmark(
            @AuthenticationPrincipal PrincipalUser principal,
            @PathVariable("productId") Long productId
    ) {
        bookmarkService.removeBookmark(principal.getUser().getUserId(), productId);
        return CommonResponse.ok(null);
    }

    @Operation(
        summary = "장터 북마크 목록 조회", 
        description = "사용자의 장터 북마크 목록을 페이징하여 조회합니다. categoryId가 0인 경우 전체 목록을 조회합니다.",
        parameters = {
            @Parameter(name = "categoryId", description = "카테고리 ID (0: 전체 조회)", required = true, in = ParameterIn.QUERY),
            @Parameter(name = "page", description = "페이지 번호 (0부터 시작)", in = ParameterIn.QUERY),
            @Parameter(name = "size", description = "페이지 크기", in = ParameterIn.QUERY),
            @Parameter(name = "sort", description = "정렬 기준 (예: createdAt,DESC)", in = ParameterIn.QUERY)
        }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "북마크 목록 조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 카테고리")
    })
    @GetMapping
    public CommonResponse<CustomPageResponseDTO<BookmarkListResponseDTO>> getBookmarkList(
            @AuthenticationPrincipal PrincipalUser principal,
            @RequestParam(name = "categoryId", defaultValue = "0") Long categoryId,
            @ParameterObject
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<BookmarkListResponseDTO> bookmarks = bookmarkService.getBookmarkList(
            principal.getUser().getUserId(), 
            categoryId, 
            pageable
        );
        return CommonResponse.ok(CustomPageResponseDTO.of(bookmarks));
    }
}