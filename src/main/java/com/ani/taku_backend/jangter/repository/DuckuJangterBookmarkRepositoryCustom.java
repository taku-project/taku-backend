package com.ani.taku_backend.jangter.repository;

import com.ani.taku_backend.jangter.model.dto.BookmarkListResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DuckuJangterBookmarkRepositoryCustom {

    Page<BookmarkListResponseDTO> findBookmarksByUserIdWithPaging(Long userId, Long categoryId, Pageable pageable);

    List<BookmarkListResponseDTO> findBookmarksByUserIdAndCategoryId(Long userId, Long categoryId);
}