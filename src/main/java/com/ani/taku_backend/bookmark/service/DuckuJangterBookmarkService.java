package com.ani.taku_backend.bookmark.service;

import com.ani.taku_backend.bookmark.domain.dto.DuckuJangterBookmarkResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DuckuJangterBookmarkService {
    void addBookmark(Long userId, Long productId);
    void removeBookmark(Long userId, Long productId);
    Page<DuckuJangterBookmarkResponseDTO> getBookmarks(Long userId, Pageable pageable);
    Page<DuckuJangterBookmarkResponseDTO> getBookmarksByCategory(Long userId, String category, Pageable pageable);
} 