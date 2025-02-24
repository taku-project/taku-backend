package com.ani.taku_backend.post.model.dto;

import com.ani.taku_backend.comments.model.dto.CommentsResponseDTO;
import com.ani.taku_backend.post.model.entity.Post;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class PostDetailResponseDTO {

    @Schema(description = "게시글 ID")
    private final Long postId;
    
    @Schema(description = "작성자 닉네임")
    private final String authorNickname;
    
    @Schema(description = "작성자 프로필 이미지 URL")
    private final String authorProfileUrl;
    
    @Schema(description = "게시글 제목")
    private final String title;
    
    @Schema(description = "게시글 본문")
    private final String content;

    @Schema(description = "저장된 게시글 시간(update되면 update된 시간 반영)", example = "2025-08-37 20:41")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", shape = JsonFormat.Shape.STRING)  // 테스트 해보기
    private final LocalDateTime updateAt;

    @Schema(description = "조회수")
    private final Long viewCount;

    @Schema(description = "현재 사용자가 게시글의 작성자인지 여부")
    private final boolean owner;

    @Schema(description = "카테고리ID")
    private final long categoryId;

    @Schema(description = "보여줄 이미지 URL")
    private final List<String> imageUrls;

    @Schema(description = "게시글의 댓글 목록")
    private final List<CommentsResponseDTO> comments;

    @Schema(description = "좋아요 수")
    private final long likeCount;

    @Schema(description = "현재 사용자가 좋아요를 눌렀는지 여부")
    private final boolean isLiked;

    @Schema(description = "총 댓글 수 (댓글 + 대댓글)")
    private final long commentCount;

    public PostDetailResponseDTO(Post post, boolean owner, List<CommentsResponseDTO> comments, 
                               long likeCount, boolean isLiked, long commentCount) {
        this.postId = post.getId();
        this.authorNickname = post.getUser().getNickname();
        this.authorProfileUrl = post.getUser().getProfileImg();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.updateAt = post.getUpdatedAt();
        this.viewCount = post.getViews();
        this.categoryId = post.getCategory().getId();
        this.owner = owner;
        this.likeCount = likeCount;
        this.isLiked = isLiked;
        this.commentCount = commentCount;

        this.imageUrls = post.getCommunityImages().stream()
                .map(communityImage -> communityImage.getImage().getImageUrl())
                .toList();
        this.comments = comments;
    }
}