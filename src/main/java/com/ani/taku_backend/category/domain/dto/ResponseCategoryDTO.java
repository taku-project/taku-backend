package com.ani.taku_backend.category.domain.dto;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.ani.taku_backend.category.domain.entity.Category;
import com.ani.taku_backend.category.domain.entity.CategoryGenre;
import com.ani.taku_backend.category.domain.entity.CategoryImage;

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
    private boolean isBookmark;
    private List<CategoryImageDTO> categoryImages;
    private List<CategoryGenreDTO> categoryGenres;


    public static ResponseCategoryDTO of(Category category, boolean hasBookmark) {

        return ResponseCategoryDTO.builder()
            .id(category.getId())
            .name(category.getName())
            .status(category.getStatus().name())
            .createdType(category.getCreatedType().name())
            .viewCount(category.getViewCount())
            .isBookmark(hasBookmark)
            .categoryImages(Collections.singletonList(CategoryImageDTO.of(category.getCategoryImage())))
            .categoryGenres(category.getCategoryGenres().stream().map(CategoryGenreDTO::of).collect(Collectors.toList()))
            .build();
    }

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

        public static CategoryImageDTO of(CategoryImage categoryImage) {
            return CategoryImageDTO.builder()
                .id(categoryImage.getId())
                .imageUrl(categoryImage.getImage().getImageUrl())
                .fileName(categoryImage.getImage().getFileName())
                .originalFileName(categoryImage.getImage().getOriginalName())
                .build();
        }
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

        public static CategoryGenreDTO of(CategoryGenre categoryGenre) {
            return CategoryGenreDTO.builder()
                .id(categoryGenre.getGenre().getId())
                .name(categoryGenre.getGenre().getGenreName())
                .build();
        }
    }
}

