package com.ani.taku_backend.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ani.taku_backend.common.model.entity.Image;

public interface ImageRepository extends JpaRepository<Image, Long> {

}
