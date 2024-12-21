package com.ani.taku_backend.post.model.dto;

import com.ani.taku_backend.common.model.entity.Image;
import com.ani.taku_backend.post.model.entity.Post;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PostListResponseDTO {

    private Long id;
    private Long userId;     // User 객체 대신 ID만 포함
    private Long categoryId; // Category 객체 대신 ID만 포함

    private String title;
    private String content;

    private String imageUrl;   // Image 링크를 응답
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Long views;
    private Long likes;

    public PostListResponseDTO(Post post) {
        this.id = post.getId();
        this.userId = post.getUser().getUserId();
        this.categoryId = post.getCategory().getId();
        this.title = post.getTitle();
        this.content = post.getContent();

        // 이미지가 여러 장일 경우 첫 번째 이미지 URL 가져오기
        this.imageUrl = post.getCommunityImages()
                .stream()
                .findFirst()
                .map(communityImage -> communityImage.getImage().getImageUrl())
                .orElse(null);


        this.createdAt = post.getCreatedAt();
        this.updatedAt = post.getUpdatedAt();
        this.views = post.getViews();
        this.likes = post.getLikes();
    }
}
