package com.ani.taku_backend.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ani.taku_backend.user.model.entity.UserImage;

public interface UserImageRepository extends JpaRepository<UserImage, Long> {

}
