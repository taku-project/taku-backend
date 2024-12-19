package com.ani.taku_backend.post.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PostDTO {

    private Long id;
    private Long userId; // User 객체 대신 ID만 포함
    private Long categoryId; // Category 객체 대신 ID만 포함
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long views;
    private Long likes;

}
