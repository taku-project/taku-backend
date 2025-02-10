package com.ani.taku_backend.category.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Schema(description = "카테고리 생성 form-data 요청 객체")
@Data
public class RequestCategoryCreateDTO {
    @NotEmpty
    @Schema(name = "category_name", description = "카테고리 이름", example = "원피스")
    @JsonProperty("category_name")
    private String name;

    @NotEmpty
    @Schema(name = "ani_genre_id", description = "애니 장르 ID 목록", example = "[1, 2, 3]")
    @JsonProperty("ani_genre_id")
    private List<Long> aniGenreId;

    @NotNull
    @Schema(name = "image", description = "카테고리 썸네일 이미지 파일", type = "string", format = "binary")
    @JsonProperty("image")
    private MultipartFile image;
}
