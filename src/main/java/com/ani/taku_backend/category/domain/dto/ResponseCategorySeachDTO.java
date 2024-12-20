package com.ani.taku_backend.category.domain.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.ani.taku_backend.category.domain.entity.Category;
import com.ani.taku_backend.category.domain.entity.CategoryGenre;
import com.ani.taku_backend.category.domain.entity.CategoryImage;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
public class ResponseCategorySeachDTO {
    private Long id;    // 카테고리 아이디
    private String name;    // 카테고리 이름
    private LocalDateTime createdAt;    // 카테고리 생성일
    private LocalDateTime updatedAt;    // 카테고리 수정일
    private String status;    // 카테고리 상태
    private Long viewCount;    // 조회수

    private Long categoryCreateUserId;    // 카테고리 생성 유저 아이디
    private String categoryCreateNickname;    // 카테고리 생성 유저 닉네임
    private String categoryCreateUserProfileImageUrl;    // 카테고리 생성 유저 프로필 이미지 주소

    private Long imageId;    // 카테고리 이미지 아이디
    private String imageUrl;    // 카테고리 이미지 주소

    private Long[] genreId;    // 장르 아이디
    private String[] genreName;    // 장르 이름
    
}
