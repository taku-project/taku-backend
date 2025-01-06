package com.ani.taku_backend.post.model.dto;

import com.ani.taku_backend.common.model.entity.Image;
import com.ani.taku_backend.post.model.entity.Post;
import com.fasterxml.jackson.annotation.JsonFormat;
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

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", shape = JsonFormat.Shape.STRING)  // 테스트 해보기
    private LocalDateTime updatedAt;

    private long views;
    private long postCount; // 게시글 수

    public PostListResponseDTO(Post post, long postCount) {
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

        this.updatedAt = post.getUpdatedAt();
        this.views = post.getViews();
        this.postCount = postCount;
    }
}
