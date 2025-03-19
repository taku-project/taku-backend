package com.ani.taku_backend.category.dto;

import java.util.List;

import lombok.Data;

@Data
public class CategorySearchReqDTO {
    
    private String name;
    private List<Long> genreIds;
}
