package com.ani.taku_backend.jangter.repository;

import com.ani.taku_backend.jangter.model.entity.DuckuJangter;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface DuckuJangterRepository extends JpaRepository<DuckuJangter, Long> {

    @Modifying
    @Query("update DuckuJangter d set d.viewCount = d.viewCount + :viewCount where d.id = :productId")
    void updateViewCount(@Param("productId") Long productId, @Param("viewCount") Long viewCount);

    List<DuckuJangter> findByDeletedAtIsNull();
}
