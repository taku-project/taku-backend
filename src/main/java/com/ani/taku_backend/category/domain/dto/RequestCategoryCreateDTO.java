package com.ani.taku_backend.category.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class RequestCategoryCreateDTO {

    @JsonProperty("category_name")
    private String name;

    @JsonProperty("ani_genre_id")
    private Long[] aniGenreId;
}
