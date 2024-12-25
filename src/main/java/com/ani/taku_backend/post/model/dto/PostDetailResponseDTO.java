package com.ani.taku_backend.post.model.dto;

import com.ani.taku_backend.post.model.entity.Post;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PostDetailResponseDTO {

    private final Long postId;
    private final String title;
    private final String content;
    private final LocalDateTime createdAt;
    private final Long viewCount;
    private final boolean owner; //내 개시물 여부

    public PostDetailResponseDTO(Post post, boolean owner) {
        this.postId = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.createdAt = post.getCreatedAt();
        this.viewCount = post.getViews();
        this.owner = owner;
    }
}