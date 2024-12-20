package com.ani.taku_backend.post.model.dto;

import com.ani.taku_backend.post.model.entity.Post;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PostResponseDTO {

    private Long id;
    private Long userId; // User 객체 대신 ID만 포함
    private Long categoryId; // Category 객체 대신 ID만 포함
    private String title;
    private String content;

//    private Imege imegeId; // 이미지 Entity와 연동한뒤 기능 추가 개발

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long views;
    private Long likes;

    public PostResponseDTO(Post post) {
        this.id = post.getId();
        this.userId = post.getUser().getUserId();
        this.categoryId = post.getCategory().getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.createdAt = post.getCreatedAt();
        this.updatedAt = post.getUpdatedAt();
        this.views = post.getViews();
        this.likes = post.getLikes();
    }
}
