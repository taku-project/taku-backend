package com.ani.taku_backend.admin.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.ani.taku_backend.admin.domain.entity.ProfanityFilter;

public interface ProfanityFilterRepository extends JpaRepository<ProfanityFilter, Long> , JpaSpecificationExecutor<ProfanityFilter> {

    // 키워드로 검색 , like (x)
    Optional<ProfanityFilter> findByKeyword(String keyword);
    
}
