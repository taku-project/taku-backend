package com.ani.taku_backend.user.model.entity;

import java.time.LocalDateTime;
import java.util.List;

import com.ani.taku_backend.category.domain.entity.CategoryImage;
import com.ani.taku_backend.common.baseEntity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 이미지 엔티티
 */
@Entity
@Table(name = "images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Image extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uploader_id")
    private Long uploaderId;

    @Column(name = "file_name" , length = 255)
    private String fileName;

    @Column(name = "image_url" , length = 500)
    private String imageUrl;

    @Column(name = "original_name" , length = 255)
    private String originalName;

    @Column(name = "file_type" , length = 50)
    private String fileType;

    @Column(name = "file_size")
    private Integer fileSize;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // 유저 이미지
    @OneToMany(mappedBy = "image")
    private List<UserImage> userImages;
    // 키테고리 이미지
    @OneToMany(mappedBy = "image")
    private List<CategoryImage> categoryImages;

    // TODO : 커뮤니티 이미지

}
