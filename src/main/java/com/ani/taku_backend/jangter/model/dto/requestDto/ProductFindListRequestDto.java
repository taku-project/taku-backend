package com.ani.taku_backend.jangter.model.dto.requestDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ProductFindListRequestDto {

    @Schema(description = "정렬 기준 (가격: PRICE, 날짜: DAY)", example = "PRICE")
    private String sort;

    @Schema(description = "정렬 기준 오른차순 (ACS) 내림 차순(DESC)")
    private String order;

    @Schema(description = "마지막으로 본 id", example = "0")
    private Long lastId;

    @Schema(description = "페이지 크기", example = "10")
    private int size;

    @Schema(description = "최소 가격 필터", example = "10000")
    private Integer minPrice;

    @Schema(description = "최대 가격 필터", example = "50000")
    private Integer maxPrice;

    @Schema(description = "카테고리 필터", example = "가전")
    private String categories;

    @Schema(description = "검색", example = "게토 피규어")
    private String searchKeyword;

}
