package com.ani.taku_backend.category.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ani.taku_backend.category.domain.entity.CategoryImage;

public interface CategoryImageRepository extends JpaRepository<CategoryImage, Long> {

}
