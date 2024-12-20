package com.ani.taku_backend.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ani.taku_backend.user.model.entity.BlackUser;

public interface BlackUserRepository extends JpaRepository<BlackUser, Long> {
    
}
