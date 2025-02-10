package com.ani.taku_backend.post.model.dto;

import com.ani.taku_backend.common.exception.DuckwhoException;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import static com.ani.taku_backend.common.exception.ErrorCode.INVALID_INPUT_VALUE;

@Data
public class PostListRequestDTO {

    @Schema(description = "검색어")
    private String keyword;

    @Schema(description = "카테고리 ID(기본값 1)", defaultValue = "1")
    private long categoryId;

    public void postListRequestValidate() {
        if (categoryId <= 0) {
            throw new DuckwhoException(INVALID_INPUT_VALUE);
        }
    }

}
