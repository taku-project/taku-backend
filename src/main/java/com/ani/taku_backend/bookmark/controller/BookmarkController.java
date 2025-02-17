package com.ani.taku_backend.bookmark.controller;

import com.ani.taku_backend.bookmark.domain.Bookmark;
import com.ani.taku_backend.bookmark.service.BookmarkService;
import com.ani.taku_backend.common.response.ResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jangter/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @PostMapping("/{productId}")
    public ResponseEntity<ResponseDTO<Void>> addBookmark(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long productId) {
        bookmarkService.addJangterBookmark(userId, productId);
        return ResponseEntity.ok(ResponseDTO.success());
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<ResponseDTO<Void>> removeBookmark(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long productId) {
        bookmarkService.removeJangterBookmark(userId, productId);
        return ResponseEntity.ok(ResponseDTO.success());
    }

    @GetMapping
    public ResponseEntity<ResponseDTO<Page<Bookmark>>> getBookmarks(
            @AuthenticationPrincipal Long userId,
            @PageableDefault Pageable pageable) {
        return ResponseEntity.ok(ResponseDTO.success(
                bookmarkService.getJangterBookmarks(userId, pageable)));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<ResponseDTO<Page<Bookmark>>> getBookmarksByCategory(
            @AuthenticationPrincipal Long userId,
            @PathVariable String category,
            @PageableDefault Pageable pageable) {
        return ResponseEntity.ok(ResponseDTO.success(
                bookmarkService.getJangterBookmarksByCategory(userId, category, pageable)));
    }
} 