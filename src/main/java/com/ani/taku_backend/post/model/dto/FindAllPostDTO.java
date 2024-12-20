package com.ani.taku_backend.post.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class FindAllPostDTO {

    private Long id;
    private Long userId; // User 객체 대신 ID만 포함
    private Long categoryId; // Category 객체 대신 ID만 포함
    private String title;
    private String content;

//    private Imege imegeId; // 이미지 Entity와 연동한뒤 기능 추가 개발

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long views;
    private Long likes;

}
