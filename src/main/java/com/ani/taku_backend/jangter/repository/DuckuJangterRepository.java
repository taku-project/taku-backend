package com.ani.taku_backend.jangter.repository;

import com.ani.taku_backend.jangter.model.entity.DuckuJangter;
import com.ani.taku_backend.jangter.model.dto.ProductStatusDTO;
import com.ani.taku_backend.jangter.model.dto.ProductImageDTO;
import com.ani.taku_backend.jangter.dto.ProductAggregateDTO;

import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;



public interface DuckuJangterRepository extends JpaRepository<DuckuJangter, Long>, DuckuJangterRepositoryCustom {

    @Modifying
    @Query("update DuckuJangter d set d.viewCount = d.viewCount + :viewCount where d.id = :productId")
    void updateViewCount(@Param("productId") Long productId, @Param("viewCount") Long viewCount);

    List<DuckuJangter> findByDeletedAtIsNull();

    @Query("select d from DuckuJangter d where d.buyUser.userId = :userId")
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

    @Query("SELECT new com.ani.taku_backend.jangter.model.dto.ProductStatusDTO(" +
           "d, m) FROM DuckuJangter d " +
           "LEFT JOIN MarketPriceStats m ON m.product = d " +
           "AND m.registeredDate = (" +
           "    SELECT MAX(m2.registeredDate) " +
           "    FROM MarketPriceStats m2 " +
           "    WHERE m2.product = d" +
           ") " +
           "WHERE d.id = :productId")
    Optional<ProductStatusDTO> findProductWithLatestStats(@Param("productId") Long productId);

    @EntityGraph(attributePaths = {"jangterImages", "jangterImages.image", "user", "itemCategories"})
    Optional<DuckuJangter> findWithDetailsById(Long id);

    /**
     * 상품 ID 목록으로 상품 이미지 정보를 조회합니다.
     * 각 상품의 첫 번째 이미지만 반환합니다.
     * 
     * @param productIds 상품 ID 목록
     * @return 상품 ID와 이미지 URL 정보의 목록
     */
    @Query("SELECT new com.ani.taku_backend.jangter.model.dto.ProductImageDTO(j.id, COALESCE((SELECT ji.image.imageUrl FROM JangterImages ji WHERE ji.duckuJangter.id = j.id ORDER BY ji.id ASC LIMIT 1), null)) " +
           "FROM DuckuJangter j WHERE j.id IN :productIds")
    List<ProductImageDTO> findProductImagesById(@Param("productIds") List<Long> productIds);

    // 상품 및 관련 정보를 한 번에 조회하는 최적화된 쿼리
    @Query("SELECT new com.ani.taku_backend.jangter.dto.ProductAggregateDTO(" +
           "j, " +
           "(SELECT ji.image.imageUrl FROM JangterImages ji WHERE ji.duckuJangter.id = j.id ORDER BY ji.id ASC LIMIT 1), " +
           "j.title, j.price, j.user.id) " +
           "FROM DuckuJangter j WHERE j.id = :id")
    Optional<ProductAggregateDTO> findProductAggregateById(@Param("id") Long id);
}
