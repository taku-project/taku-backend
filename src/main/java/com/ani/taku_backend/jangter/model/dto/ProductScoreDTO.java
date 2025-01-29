package com.ani.taku_backend.jangter.model.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductScoreDTO extends ProductViewAndBookmarkDTO {

    private BigDecimal totalScore;
    private BigDecimal viewScore;
    private BigDecimal bookmarkScore;

    private int rank;

    public ProductScoreDTO(ProductViewAndBookmarkDTO productViewAndBookmarkDTO, BigDecimal totalScore , BigDecimal viewScore, BigDecimal bookmarkScore) {
        super(productViewAndBookmarkDTO.getProductId(), productViewAndBookmarkDTO.getViewCount(), productViewAndBookmarkDTO.getIsBookmarked());
        this.totalScore = totalScore;
        this.viewScore = viewScore;
        this.bookmarkScore = bookmarkScore;
    }
}
