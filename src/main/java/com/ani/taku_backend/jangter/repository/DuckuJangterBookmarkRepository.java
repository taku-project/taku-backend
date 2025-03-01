package com.ani.taku_backend.jangter.repository;

import com.ani.taku_backend.jangter.model.entity.DuckuJangterBookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface DuckuJangterBookmarkRepository extends JpaRepository<DuckuJangterBookmark, Long>, DuckuJangterBookmarkRepositoryCustom {

    // 활성 상태와 관계없이 북마크 조회
    Optional<DuckuJangterBookmark> findByUserUserIdAndJangterId(Long userId, Long jangterId);

    // 사용자의 모든 활성화된 북마크 목록 조회
    List<DuckuJangterBookmark> findByUserUserIdAndIsActiveTrue(Long userId);
;
}