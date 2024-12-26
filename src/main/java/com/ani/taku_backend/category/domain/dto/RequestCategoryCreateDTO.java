package com.ani.taku_backend.category.domain.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class RequestCategoryCreateDTO {

    @JsonProperty("category_name")
    private String name;

    @JsonProperty("ani_genre_id")
    private List<Long> aniGenreId;
}
