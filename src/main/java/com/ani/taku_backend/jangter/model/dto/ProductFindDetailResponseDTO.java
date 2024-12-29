package com.ani.taku_backend.jangter.model.dto;

import com.ani.taku_backend.common.enums.StatusType;
import com.ani.taku_backend.jangter.model.entity.DuckuJangter;
import lombok.Data;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductFindDetailResponseDTO {

    private String title;
    private String description;
    private BigDecimal price;
    private StatusType status;
    private LocalDateTime createdAt;
    private Long viewCount;

    public ProductFindDetailResponseDTO(DuckuJangter duckuJangter, StatusType status, Long addViewCount) {
        this.title = duckuJangter.getTitle();
        this.description = duckuJangter.getDescription();
        this.price = duckuJangter.getPrice();
        this.status = status;
        this.createdAt = duckuJangter.getCreatedAt();
        this.viewCount = duckuJangter.getViewCount() + (addViewCount != null ? addViewCount : 0L);
    }
}
