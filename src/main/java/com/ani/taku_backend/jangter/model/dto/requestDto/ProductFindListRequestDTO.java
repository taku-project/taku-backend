package com.ani.taku_backend.jangter.model.dto.requestDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProductFindListRequestDTO {

    @Schema(description = "정렬 기준 (가격: price, 날짜: day)", example = "price")
    private String sort;

    @Schema(description = "정렬 기준 오른차순 (asc) 내림 차순(desc)", example = "asc")
    private String order;


    @Schema(description = "마지막으로 본 id", example = "0")
    private Long lastId;

    @Schema(description = "페이지 크기", example = "10")
    private int size;

    @Schema(description = "최소 가격 필터", example = "100",nullable = true)
    private Integer minPrice;

    @Schema(description = "최대 가격 필터", example = "50000000",nullable = true)
    private Integer maxPrice;

    @Schema(description = "카테고리 ID()", example = "1", nullable = true)
    private long categoryId;

    @Schema(description = "검색", example = "", nullable = true)
    private String searchKeyword;

}
