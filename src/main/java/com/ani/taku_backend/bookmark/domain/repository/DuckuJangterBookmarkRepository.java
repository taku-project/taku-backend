package com.ani.taku_backend.bookmark.domain.repository;

import com.ani.taku_backend.bookmark.domain.Bookmark;
import com.ani.taku_backend.jangter.model.entity.DuckuJangter;
import com.ani.taku_backend.jangter.model.entity.DuckuJangterBookmark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DuckuJangterBookmarkRepository extends JpaRepository<DuckuJangterBookmark, Long> {
    
    Optional<DuckuJangterBookmark> findByBookmarkAndJangter(Bookmark bookmark, DuckuJangter jangter);

    @Query("SELECT db FROM DuckuJangterBookmark db " +
           "JOIN FETCH db.jangter j " +
           "JOIN FETCH j.itemCategories " +
           "WHERE db.bookmark = :bookmark")
    Page<DuckuJangterBookmark> findAllByBookmarkWithJangter(@Param("bookmark") Bookmark bookmark, Pageable pageable);

    @Query("SELECT db FROM DuckuJangterBookmark db " +
           "JOIN FETCH db.jangter j " +
           "JOIN FETCH j.itemCategories ic " +
           "WHERE db.bookmark = :bookmark " +
           "AND ic.name = :category")
    Page<DuckuJangterBookmark> findAllByBookmarkAndCategoryWithJangter(
            @Param("bookmark") Bookmark bookmark,
            @Param("category") String category,
            Pageable pageable);

    void deleteByBookmarkAndJangter(Bookmark bookmark, DuckuJangter jangter);
} 