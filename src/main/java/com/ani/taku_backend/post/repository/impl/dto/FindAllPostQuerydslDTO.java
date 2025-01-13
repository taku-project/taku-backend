package com.ani.taku_backend.post.repository.impl.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FindAllPostQuerydslDTO {
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

    @QueryProjection
    public FindAllPostQuerydslDTO(long postId, long userId, long categoryId,
                                  String title, String content, String imageUrl,
                                  LocalDateTime updatedAt, long views) {
        this.id = postId;
        this.userId = userId;
        this.categoryId = categoryId;
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
        this.updatedAt = updatedAt;
        this.views = views;
    }
}
