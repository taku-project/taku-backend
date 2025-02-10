package com.ani.taku_backend.category.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class RequestCategoryCreateDTO {
    @NotEmpty
    @Schema(name = "name", description = "카테고리 이름")
    @JsonProperty("category_name")
    private String name;

    @NotEmpty
    @Schema(name = "aniGenreId", description = "애니 장르 ID")
    @JsonProperty("ani_genre_id")
    private List<Long> aniGenreId;

    @NotNull
    @Schema(name = "image", description = "카테고리 썸네일 이미지 파일")
    @JsonProperty("image")
    private MultipartFile image;
}
