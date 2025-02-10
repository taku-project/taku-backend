package com.ani.taku_backend.bookmark.domain.repository;

import com.ani.taku_backend.bookmark.domain.CategoryBookmark;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryBookmarkRepository extends JpaRepository<CategoryBookmark, Long> {
    Optional<CategoryBookmark> findByCategoryIdAndUserUserId(Long categoryId, Long userId);

}
