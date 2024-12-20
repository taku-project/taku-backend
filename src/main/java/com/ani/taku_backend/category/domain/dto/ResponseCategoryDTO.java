package com.ani.taku_backend.category.domain.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ResponseCategoryDTO {
    private Long id;
    private String name;
    private String status;
    private String createdType;
    private Long viewCount;
    private List<CategoryImageDTO> categoryImages;
    private List<CategoryGenreDTO> categoryGenres;
    
    @Getter
    @Builder
    @Setter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryImageDTO {
        private Long id;
        private String imageUrl;
        private String fileName;
        private String originalFileName;
    }
    
    @Getter
    @Setter
    @Builder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryGenreDTO {
        private Long id;
        private String name;
    }
}

