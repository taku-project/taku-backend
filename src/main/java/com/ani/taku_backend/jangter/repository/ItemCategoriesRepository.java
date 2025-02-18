package com.ani.taku_backend.jangter.repository;

import com.ani.taku_backend.jangter.model.entity.ItemCategories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ItemCategoriesRepository extends JpaRepository<ItemCategories, Long> {
    @Query("SELECT ic FROM ItemCategories ic WHERE ic.name = :name")
    Optional<ItemCategories> findByName(@Param("name") String name);
}
