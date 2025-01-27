package com.ani.taku_backend.admin.category.repository;

import com.ani.taku_backend.category.domain.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminCategoryRepository extends JpaRepository<Category, Long>, CustomAdminCategoryRepository {

}
