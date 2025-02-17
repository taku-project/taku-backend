package com.ani.taku_backend.category_bookmark.domain.repository;

import com.ani.taku_backend.category_bookmark.domain.CategoryBookmark;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryBookmarkRepository extends JpaRepository<CategoryBookmark, Long>, CustomCategoryBookmarkRepository {
    Optional<CategoryBookmark> findByCategoryIdAndUserUserId(Long categoryId, Long userId);

    Optional<CategoryBookmark> findByIdAndUserUserId(Long categoryId, Long userId);
}
