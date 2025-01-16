package com.ani.taku_backend.admin.category.dto.res;

import com.ani.taku_backend.category.domain.dto.AniGenreResDTO;
import com.ani.taku_backend.category.domain.entity.Category;
import com.ani.taku_backend.category.domain.entity.CategoryImage;
import com.ani.taku_backend.category.domain.entity.CategoryStatus;
import com.ani.taku_backend.common.enums.UserRole;
import com.querydsl.core.annotations.QueryProjection;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminCategoryResDTO {
    private Long id;
    private String name;
    private UserRole createdType;
    private CategoryStatus status;
    private String imageUrl;
    private List<AniGenreResDTO> aniCategoryList;

    @QueryProjection
    public AdminCategoryResDTO(Long id, String name, UserRole createdType, CategoryStatus status, String imageUrl, List<AniGenreResDTO> aniCategoryList) {
        this.id = id;
        this.name = name;
        this.createdType = createdType;
        this.status = status;
        this.imageUrl = imageUrl;
        this.aniCategoryList = aniCategoryList;
    }

    public AdminCategoryResDTO(Category category) {
        this.id = category.getId();
        this.name = category.getName();
        this.createdType = category.getCreatedType();
        this.status = category.getStatus();
        CategoryImage categoryImage = category.getCategoryImage();
        this.imageUrl = categoryImage != null ? categoryImage.getImage().getImageUrl() : "";
        this.aniCategoryList = category.getCategoryGenres().stream()
                .map(categoryGenre ->
                    new AniGenreResDTO(categoryGenre.getId(), categoryGenre.getGenre().getGenreName())
                ).toList();
    }
}
