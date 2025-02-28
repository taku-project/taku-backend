package com.ani.taku_backend.jangter.service;

import com.ani.taku_backend.jangter.model.dto.BookmarkListResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DuckuJangterBookmarkService {

    /**
     * 장터 상품을 북마크에 추가합니다.
     * @param userId 사용자 ID
     * @param productId 상품 ID
     */
    void addBookmark(Long userId, Long productId);

    /**
     * 장터 상품을 북마크에서 제거합니다.
     * @param userId 사용자 ID
     * @param productId 상품 ID
     */
    void removeBookmark(Long userId, Long productId);

    /**
     * 사용자의 장터 북마크 목록을 조회합니다.
     * @param userId 사용자 ID
     * @param categoryId 카테고리 ID (0: 전체 조회)
     * @param pageable 페이징 정보
     * @return 북마크 목록
     */
    Page<BookmarkListResponseDTO> getBookmarkList(Long userId, Long categoryId, Pageable pageable);
}