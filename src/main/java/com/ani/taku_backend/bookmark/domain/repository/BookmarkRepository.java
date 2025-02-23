package com.ani.taku_backend.bookmark.domain.repository;

import com.ani.taku_backend.bookmark.domain.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    Optional<Bookmark> findByUser_UserId(Long userId);

    List<Bookmark> findByUserId(Long userId);

    @Query("SELECT DISTINCT b FROM Bookmark b " +
            "LEFT JOIN FETCH b.duckuJangterBookmarks " +
            "WHERE b.user.userId = :userId")
    List<Bookmark> findByUserIdWithJangterAndCategories(@Param("userId") Long userId);
}