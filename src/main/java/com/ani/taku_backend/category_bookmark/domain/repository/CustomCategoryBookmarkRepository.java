package com.ani.taku_backend.category_bookmark.domain.repository;

import com.ani.taku_backend.category_bookmark.domain.CategoryBookmark;
import com.ani.taku_backend.category_bookmark.dto.res.CategoryBookmarkDTO;

import java.util.List;

public interface CustomCategoryBookmarkRepository {
    List<CategoryBookmarkDTO> findByUserUserId(Long userId);
}
