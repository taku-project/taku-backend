package com.ani.taku_backend.post.model.dto;

import com.ani.taku_backend.common.enums.SortFilterType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class FindAllPostParamDTO {

    @Schema(description = "정렬 기준", defaultValue = "latest")
    private SortFilterType filter;

    @Schema(description = "정렬 기준의 마지막 값")
    private Long lastValue;

    @Schema(description = "정렬 방향(true = 오름차순, false = 내림차순)", defaultValue = "false")
    private boolean isAsc = false;

    @Schema(description = "페이지당 항목 수", defaultValue = "20")
    private int limit = 20;

    @Schema(description = "검색어")
    private String keyword;
}
