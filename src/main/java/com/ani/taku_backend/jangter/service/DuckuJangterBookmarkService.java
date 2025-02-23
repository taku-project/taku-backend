package com.ani.taku_backend.jangter.service;

import com.ani.taku_backend.jangter.model.dto.BookmarkListResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DuckuJangterBookmarkService {
    /**
     * 사용자가 상품을 찜 목록에 추가합니다.
     */
    void addBookmark(Long userId, Long jangterId);

    /**
     * 사용자가 찜 목록에서 상품을 삭제합니다.
     */
    void removeBookmark(Long userId, Long jangterId);

    /**
     * 사용자의 찜 목록을 페이징 및 정렬 조건에 맞게 조회합니다.
     */
    Page<BookmarkListResponseDTO> getBookmarkList(Long userId, Long categoryId, Pageable pageable);

    /**
     * 사용자의 특정 카테고리 찜 목록을 전체 조회합니다.
     */
    List<BookmarkListResponseDTO> getBookmarkListByCategory(Long userId, Long categoryId);
}