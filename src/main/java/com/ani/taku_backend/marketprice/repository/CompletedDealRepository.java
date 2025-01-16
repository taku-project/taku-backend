package com.ani.taku_backend.marketprice.repository;

import com.ani.taku_backend.marketprice.model.entity.CompletedDeal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompletedDealRepository extends JpaRepository<CompletedDeal, Long>, CompletedDealQueryRepository {
    List<CompletedDeal> findByCategoryNameAndCreatedAtAfterOrderByCreatedAtDesc(
            String categoryName,
            LocalDateTime createdAt
    );
}