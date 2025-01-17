package com.ani.taku_backend.post.model.dto;

import com.ani.taku_backend.common.enums.SortFilterType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class PostListRequestDTO {

    @Schema(description = "정렬 기준 id = latest 조회수 = views 좋아요 = likes", defaultValue = "latest")
    private String sortFilterType = SortFilterType.LATEST.getValue();

    @Schema(description = "정렬 기준(id, 조회수, 좋아요)의 마지막 값", defaultValue = "0")
    private long lastValue;

    @Schema(description = "정렬 방향(true = 오름차순, false = 내림차순)", defaultValue = "false")
    private boolean asc = false;

    @Schema(description = "페이지당 항목 수", defaultValue = "20")
    private int limit = 20;

    @Schema(description = "검색어")
    private String keyword;

    @Schema(description = "카테고리 ID(현재 69, 70에 더미데이터 존재)", defaultValue = "69")
    private long categoryId;
}
