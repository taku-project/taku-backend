package com.ani.taku_backend.jangter.repository;

import com.ani.taku_backend.jangter.model.entity.DuckuJangterBookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface DuckuJangterBookmarkRepository extends JpaRepository<DuckuJangterBookmark, Long>, DuckuJangterBookmarkRepositoryCustom {
    Optional<DuckuJangterBookmark> findByBookmark_User_UserIdAndJangter_Id(Long userId, Long jangterId);
    void deleteByBookmark_User_UserIdAndJangter_Id(Long userId, Long jangterId);
    boolean existsByBookmark_User_UserIdAndJangter_Id(Long userId, Long jangterId);
    
    Optional<DuckuJangterBookmark> findByUserUserIdAndJangterId(Long userId, Long jangterId);
    void deleteByUserUserIdAndJangterId(Long userId, Long jangterId);
    boolean existsByUserUserIdAndJangterId(Long userId, Long jangterId);
    
    List<DuckuJangterBookmark> findByUserUserIdAndIsActiveTrue(Long userId);
}