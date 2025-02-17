package com.ani.taku_backend.bookmark.controller;

import com.ani.taku_backend.bookmark.domain.dto.DuckuJangterBookmarkResponseDTO;
import com.ani.taku_backend.bookmark.service.DuckuJangterBookmarkService;
import com.ani.taku_backend.common.response.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jangter/bookmarks")
@RequiredArgsConstructor
public class DuckuJangterBookmarkController {

    private final DuckuJangterBookmarkService duckuJangterBookmarkService;

    @PostMapping("/{productId}")
    public CommonResponse<Void> addBookmark(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long productId) {
        duckuJangterBookmarkService.addBookmark(userId, productId);
        return CommonResponse.ok(null);
    }

    @DeleteMapping("/{productId}")
    public CommonResponse<Void> removeBookmark(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long productId) {
        duckuJangterBookmarkService.removeBookmark(userId, productId);
        return CommonResponse.ok(null);
    }

    @GetMapping
    public CommonResponse<Page<DuckuJangterBookmarkResponseDTO>> getBookmarks(
            @AuthenticationPrincipal Long userId,
            @PageableDefault Pageable pageable) {
        return CommonResponse.ok(
                duckuJangterBookmarkService.getBookmarks(userId, pageable));
    }

    @GetMapping("/category/{category}")
    public CommonResponse<Page<DuckuJangterBookmarkResponseDTO>> getBookmarksByCategory(
            @AuthenticationPrincipal Long userId,
            @PathVariable String category,
            @PageableDefault Pageable pageable) {
        return CommonResponse.ok(
                duckuJangterBookmarkService.getBookmarksByCategory(userId, category, pageable));
    }
} 
