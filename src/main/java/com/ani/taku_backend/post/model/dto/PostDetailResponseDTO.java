package com.ani.taku_backend.post.model.dto;

import com.ani.taku_backend.post.model.entity.Post;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

//TODO
// 댓글 기능 구현되면 연결필요
@Getter
public class PostDetailResponseDTO {

    private final Long postId;
    private final String title;
    private final String content;
    private final LocalDateTime createdAt;
    private final Long viewCount;
    private final Long likes;
    private final boolean owner;
    private final List<String> imageUrls;

    public PostDetailResponseDTO(Post post, boolean owner) {
        this.postId = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.createdAt = post.getCreatedAt();
        this.viewCount = post.getViews();
        this.likes = post.getLikes();
        this.owner = owner;

        this.imageUrls = post.getCommunityImages().stream()
                .map(communityImage -> communityImage.getImage().getImageUrl())
                .toList();
    }
}