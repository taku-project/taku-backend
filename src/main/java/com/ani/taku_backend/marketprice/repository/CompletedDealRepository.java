package com.ani.taku_backend.marketprice.repository;

import com.ani.taku_backend.jangter.model.entity.DuckuJangter;
import com.ani.taku_backend.jangter.model.entity.ItemCategories;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompletedDealRepository extends JpaRepository<DuckuJangter, Long>, CompletedDealQueryRepository {
    List<DuckuJangter> findByItemCategoryAndCreatedAtAfterOrderByCreatedAtDesc(
            ItemCategories category,
            LocalDateTime startDate
    );
}