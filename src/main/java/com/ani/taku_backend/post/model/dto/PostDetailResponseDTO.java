package com.ani.taku_backend.post.model.dto;

import com.ani.taku_backend.post.model.entity.Post;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

//TODO
// 댓글 기능 구현되면 연결필요
@Getter
public class PostDetailResponseDTO {

    @Schema(description = "게시글 ID")
    private final Long postId;
    @Schema(description = "게시글 제목")
    private final String title;
    @Schema(description = "게시글 본문")
    private final String content;

    @Schema(description = "저장된 게시글 시간(update되면 update된 시간 반영)", example = "2025-08-37 20:41")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", shape = JsonFormat.Shape.STRING)  // 테스트 해보기
    private final LocalDateTime updateAt;

    @Schema(description = "조회수")
    private final Long viewCount;

    @Schema(description = "봤는지 안봤는지? 윤정님 확인 필요")
    private final boolean owner;

    @Schema(description = "보여줄 이미지 URL")
    private final List<String> imageUrls;

    public PostDetailResponseDTO(Post post, boolean owner) {
        this.postId = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.updateAt = post.getUpdatedAt();    // 업데이트로 수정
        this.viewCount = post.getViews();
        this.owner = owner;

        this.imageUrls = post.getCommunityImages().stream()
                .map(communityImage -> communityImage.getImage().getImageUrl())
                .toList();
    }
}