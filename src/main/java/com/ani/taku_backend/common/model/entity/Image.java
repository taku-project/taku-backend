package com.ani.taku_backend.common.model.entity;

import java.time.LocalDateTime;
import java.util.List;

import com.ani.taku_backend.category.domain.entity.CategoryImage;
import com.ani.taku_backend.common.baseEntity.BaseTimeEntity;
import com.ani.taku_backend.common.model.dto.CreateImageDTO;
import com.ani.taku_backend.user.model.entity.User;
import com.ani.taku_backend.user.model.entity.UserImage;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 이미지 엔티티
 */
@Builder
@Entity
@Table(name = "images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Image extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "uploader_id", nullable = false)
    private User user;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "original_name", length = 255)
    private String originalName;

    @Column(name = "file_type", length = 50)
    private String fileType;

    @Column(name = "file_size")
    private Integer fileSize;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // TODO : 커뮤니티 이미지

    public static Image of(CreateImageDTO createImageDTO, User user) {
        return Image.builder()
                .fileName(createImageDTO.getFileName())
                .imageUrl(createImageDTO.getImageUrl())
                .originalName(createImageDTO.getOriginalFileName())
                .fileType(createImageDTO.getFileType())
                .fileSize(createImageDTO.getFileSize())
                .user(user)
                .build();
    }

    /**
     * Soft Delete 메서드
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

}
