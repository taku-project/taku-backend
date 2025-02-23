package com.ani.taku_backend.jangter.repository;

import com.ani.taku_backend.jangter.model.entity.DuckuJangterBookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DuckuJangterBookmarkRepository extends JpaRepository<DuckuJangterBookmark, Long>, DuckuJangterBookmarkRepositoryCustom {

    Optional<DuckuJangterBookmark> findByBookmark_User_UserIdAndJangter_Id(Long userId, Long jangterId);

    void deleteByBookmark_User_UserIdAndJangter_Id(Long userId, Long jangterId);

    @Query("SELECT b FROM DuckuJangterBookmark b WHERE b.bookmark.user.userId = :userId ORDER BY b.createdAt DESC")
    List<DuckuJangterBookmark> findAllByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    boolean existsByBookmark_User_UserIdAndJangter_Id(Long userId, Long jangterId);
}