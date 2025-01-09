package com.ani.taku_backend.common.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.ani.taku_backend.common.model.entity.Bookmark;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    @Query("select b from Bookmark b where b.user.id = :userId")
    List<Bookmark> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT b FROM Bookmark b " +
       "JOIN FETCH b.duckuJangterBookmarks db " +
       "JOIN FETCH db.jangter j " +
       "JOIN FETCH j.itemCategories " +
       "WHERE b.user.id = :userId")
    List<Bookmark> findByUserIdWithJangterAndCategories(@Param("userId") Long userId);
}
