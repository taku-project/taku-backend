package com.ani.taku_backend.admin.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ani.taku_backend.admin.domain.entity.ProfanityFilter;

public interface ProfanityFilterRepository extends JpaRepository<ProfanityFilter, Long> {
    
}
