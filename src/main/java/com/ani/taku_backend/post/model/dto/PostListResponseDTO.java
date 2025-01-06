package com.ani.taku_backend.post.model.dto;

import com.ani.taku_backend.common.model.entity.Image;
import com.ani.taku_backend.post.model.entity.Post;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PostListResponseDTO {
    @Schema(description = "게시글 ID")
    private Long id;

    @Schema(description = "유저 ID")
    private Long userId;     // User 객체 대신 ID만 포함

    @Schema(description = "카테고리 ID")
    private Long categoryId; // Category 객체 대신 ID만 포함

    @Schema(description = "게시글 제목")
    private String title;

    @Schema(description = "게시글 본문")
    private String content;

    @Schema(description = "저장된 이미지 URL")
    private String imageUrl;   // Image 링크를 응답

    @Schema(description = "저장된 게시글 시간(update되면 update된 시간 반영)", example = "2025-08-37 20:41")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", shape = JsonFormat.Shape.STRING)  // 테스트 해보기
    private LocalDateTime updatedAt;

    @Schema(description = "조회수")
    private long views;

    @Schema(description = "해당 카테고리에 접속된 게시글 수(삭제된 글 제외)")
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
