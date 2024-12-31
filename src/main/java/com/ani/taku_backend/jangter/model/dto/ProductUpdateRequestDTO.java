package com.ani.taku_backend.jangter.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductUpdateRequestDTO {

    private Long categoryId;
    private String title;
    private String description;
    private BigDecimal price;

    private List<String> imageUrl;

    public ProductUpdateRequestDTO(Long categoryId, String title, String description, BigDecimal price, List<String> imageUrl) {
        this.categoryId = categoryId;
        this.title = title;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
    }
}
