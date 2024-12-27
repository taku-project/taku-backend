package com.ani.taku_backend.category.domain.dto;

import java.util.List;

import lombok.Data;

@Data
public class RequestCategorySearch {
    
    private String name;
    private List<Long> genreIds;
}
