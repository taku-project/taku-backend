package com.ani.taku_backend.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ani.taku_backend.user.model.entity.UserImage;

import java.util.Optional;

public interface UserImageRepository extends JpaRepository<UserImage, Long> {


    Optional<UserImage> findByUser_UserId(Long userId);

    void deleteByImageId(Long imageId);

    void deleteByUser_UserId(Long userId);

}
