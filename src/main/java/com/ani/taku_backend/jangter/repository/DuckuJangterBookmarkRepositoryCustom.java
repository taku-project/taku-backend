package com.ani.taku_backend.jangter.repository;

import com.ani.taku_backend.jangter.model.dto.BookmarkListResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DuckuJangterBookmarkRepositoryCustom {

    /**
     * 사용자의 북마크 목록을 페이징하여 조회합니다.
     * @param userId 사용자 ID
     * @param categoryId 카테고리 ID (null: 전체 조회)
     * @param pageable 페이징 정보
     * @return 북마크 목록
     */
    Page<BookmarkListResponseDTO> findBookmarksByUserIdWithPaging(Long userId, Long categoryId, Pageable pageable);

    /**
     * 사용자의 특정 카테고리 북마크 목록을 조회합니다.
     * @param userId 사용자 ID
     * @param categoryId 카테고리 ID (null: 전체 조회)
     * @return 북마크 목록
     */
    List<BookmarkListResponseDTO> findBookmarksByUserIdAndCategoryId(Long userId, Long categoryId);
}