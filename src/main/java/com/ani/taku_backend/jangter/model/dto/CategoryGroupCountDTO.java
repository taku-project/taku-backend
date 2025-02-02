package com.ani.taku_backend.jangter.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryGroupCountDTO {
    private Long itemCategoryId;
    private String name;
    private Long count;
}

