package com.ani.taku_backend.post.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "인기글 목록의 항목 반환 객체")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PopularPostItemDTO {
    @Schema(description = "게시글 ID")
    private Long id;

    @Schema(description = "유저 ID")
    private Long userId;

    @Schema(description = "유저 이미지 URL")
    private String userImageUrl;

    @Schema(description = "카테고리 ID")
    private Long categoryId;

    @Schema(description = "카테고리 이름")
    private String categoryName;

    @Schema(description = "게시글 제목")
    private String title;

    @Schema(description = "게시글 본문")
    private String content;

    @Schema(description = "저장된 이미지 URL")
    private String imageUrl;

    @Schema(description = "저장된 게시글 시간(update되면 update된 시간 반영)", example = "2025-08-37 20:41")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", shape = JsonFormat.Shape.STRING)  // 테스트 해보기
    private LocalDateTime updatedAt;

    @Schema(description = "조회수")
    private long views;

    @Schema(description = "좋아요수")
    private long likes;

    @Schema(description = "유저 닉네임")
    private String userNickname;

    @QueryProjection
    public PopularPostItemDTO(Long id, Long userId, Long categoryId, String categoryName, String title, String content, String imageUrl, LocalDateTime updatedAt, long views, String userNickname, String userImageUrl) {
        this.id = id;
        this.userId = userId;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
        this.updatedAt = updatedAt;
        this.views = views;
        this.userNickname = userNickname;
        this.userImageUrl = userImageUrl;
    }
}