package com.ani.taku_backend.marketprice.repository;

import com.ani.taku_backend.marketprice.model.entity.CompletedDeal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CompletedDealRepository extends JpaRepository<CompletedDeal, Long>, CompletedDealQueryRepository {
    List<CompletedDeal> findByCategoryNameAndCreatedAtAfterOrderByCreatedAtDesc(
            String categoryName,
            LocalDateTime createdAt
    );

    @Query("SELECT c FROM CompletedDeal c WHERE c.product.id = :productId")
    Optional<CompletedDeal> findByProductId(@Param("productId") Long productId);
}