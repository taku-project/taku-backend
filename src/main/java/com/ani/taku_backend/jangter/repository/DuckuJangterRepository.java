package com.ani.taku_backend.jangter.repository;

import com.ani.taku_backend.common.enums.StatusType;
import com.ani.taku_backend.jangter.model.dto.CategoryGroupCountDTO;
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

    List<DuckuJangter> findByDeletedAtIsNull();

    @Query("select d from DuckuJangter d where d.buyUser.id = :userId")
    List<DuckuJangter> findByBuyUserId(@Param("userId") Long userId);

    List<DuckuJangter> findByIdIn(List<Long> productIds);

    @Query(nativeQuery = true, value = 
        "SELECT dj.* FROM ducku_jangter dj " +
        "INNER JOIN (" +
        "    SELECT d.product_id FROM ducku_jangter d " +
        "    WHERE d.status = :status " +
        "    AND d.item_category_id = :categoryId " +
        "    AND d.product_id != :productId " +
        "    AND d.deleted_at IS NULL " +
        "    ORDER BY RAND() LIMIT 5" +
        ") AS sub ON dj.product_id = sub.product_id")
    List<DuckuJangter> findByCategoryIdRandom(
        @Param("status") String status,
        @Param("categoryId") Long categoryId,
        @Param("productId") Long productId
    );

    @Query(nativeQuery = true, value = 
        "SELECT dj.* FROM ducku_jangter dj " +
        "INNER JOIN (" +
        "    SELECT d.product_id FROM ducku_jangter d " +
        "    WHERE d.status = :status " +
        "    AND d.product_id != :excludeProductId " +
        "    AND d.deleted_at IS NULL " +
        "    ORDER BY RAND() LIMIT 5" +
        ") AS sub ON dj.product_id = sub.product_id")
    List<DuckuJangter> findRandom(
        @Param("status") String status,
        @Param("excludeProductId") Long excludeProductId
    );
}
