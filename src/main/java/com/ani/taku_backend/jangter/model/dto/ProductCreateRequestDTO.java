package com.ani.taku_backend.jangter.model.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductCreateRequestDTO {


    private Long categoryId;
    private String title;
    private String description;
    private BigDecimal price;

    public ProductCreateRequestDTO(Long categoryId, String title, String description, BigDecimal price) {
        this.categoryId = categoryId;
        this.title = title;
        this.description = description;
        this.price = price;
    }
}
