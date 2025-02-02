package com.ani.taku_backend.jangter.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ani.taku_backend.jangter.model.entity.rank.JangterRankType;

public interface JangterRankTypeRepository extends JpaRepository<JangterRankType, Long> {
    
}
