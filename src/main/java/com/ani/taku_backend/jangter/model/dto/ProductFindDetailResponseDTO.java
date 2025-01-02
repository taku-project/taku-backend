package com.ani.taku_backend.jangter.model.dto;

import com.ani.taku_backend.common.enums.StatusType;
import com.ani.taku_backend.jangter.model.entity.DuckuJangter;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@Schema(description = "게시글 상세 조회 응답 DTO")
public class ProductFindDetailResponseDTO {

    @Schema(description = "장터글 제목")
    private String title;

    @Schema(description = "장터글 내용")
    private String description;

    @Schema(description = "장터글 가격")
    private BigDecimal price;

    @Schema(description = "판매 상태", example = "ACTIVE == 판매중")
    private StatusType status;

    @Schema(description = "생성일")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", shape = JsonFormat.Shape.STRING)  // 테스트 해보기
    private LocalDateTime createdAt;

    @Schema(description = "조회수")
    private long viewCount;

    @Schema(description = "이미지 리스트")
    private List<String> imageUrlList;

    public ProductFindDetailResponseDTO(DuckuJangter duckuJangter, StatusType status, Long addViewCount) {
        this.title = duckuJangter.getTitle();
        this.description = duckuJangter.getDescription();
        this.price = duckuJangter.getPrice();
        this.status = status;
        this.createdAt = duckuJangter.getCreatedAt();
        this.viewCount = duckuJangter.getViewCount() + (addViewCount != null ? addViewCount : 0L);

        // DuckuJangter와 연관된 이미지 URL 추출
        this.imageUrlList = duckuJangter.getJangterImages()
                .stream()
                .map(jangterImages -> jangterImages.getImage().getImageUrl())
                .toList();
    }
}
