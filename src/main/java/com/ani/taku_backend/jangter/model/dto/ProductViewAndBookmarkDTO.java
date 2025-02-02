package com.ani.taku_backend.jangter.model.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductViewAndBookmarkDTO {

    private Long productId;
    private Long viewCount;
    private Boolean isBookmarked;
}
