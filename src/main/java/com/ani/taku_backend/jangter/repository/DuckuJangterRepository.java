package com.ani.taku_backend.jangter.repository;

import com.ani.taku_backend.common.enums.StatusType;
import com.ani.taku_backend.jangter.model.entity.DuckuJangter;
import com.ani.taku_backend.jangter.repository.impl.DuckuJangterRepositoryCustom;

import io.lettuce.core.dynamic.annotation.Param;

import java.util.List;
import java.math.BigDecimal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface DuckuJangterRepository extends JpaRepository<DuckuJangter, Long> , DuckuJangterRepositoryCustom {

    @Modifying
    @Query("update DuckuJangter d set d.viewCount = d.viewCount + :viewCount where d.id = :productId")
    void updateViewCount(@Param("productId") Long productId, @Param("viewCount") Long viewCount);

    @Query("select d from DuckuJangter d where d.buyUser.id = :userId")
    List<DuckuJangter> findByBuyUserId(@Param("userId") Long userId);
}
