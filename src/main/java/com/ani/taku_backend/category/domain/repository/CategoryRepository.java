package com.ani.taku_backend.category.domain.repository;

import com.ani.taku_backend.category.domain.entity.Category;
import com.ani.taku_backend.category.domain.repository.impl.CustomCategoryRepository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CategoryRepository extends JpaRepository<Category, Long>, CustomCategoryRepository {


    @Query("SELECT DISTINCT c FROM Category c WHERE " +
           "FUNCTION('REGEXP_REPLACE', c.name, '[ㄱ-ㅎㅏ-ㅣ가-힣a-zA-Z0-9]', '') = :chosung OR " +
           "SUBSTRING(c.name, 1, 1) = SUBSTRING(:originalName, 1, 1) OR " +
           "REPLACE(LOWER(c.name), ' ', '') LIKE %:searchName%")
    List<Category> findSimilarNames(
        @Param("chosung") String chosung,
        @Param("searchName") String searchName,
        @Param("originalName") String originalName
    );
}
