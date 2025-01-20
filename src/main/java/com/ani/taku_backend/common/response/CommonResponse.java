package com.ani.taku_backend.common.response;

import com.ani.taku_backend.common.exception.ErrorCode;
import com.ani.taku_backend.common.exception.ExceptionDto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;

@Schema(description = "공통 응답")
public record CommonResponse<T>(
        @JsonIgnore
        HttpStatus httpStatus,

        @Schema(description = "성공 여부", example = "true")
        boolean success,

        @Schema(description = "응답 데이터")
        @Nullable T data,

        @Schema(description = "에러 정보")
        @Nullable ExceptionDto error
) {

    public static <T> CommonResponse<T> ok(@Nullable final T data) {
        return new CommonResponse<>(HttpStatus.OK, true, data, null);
    }

    public static <T> CommonResponse<T> created(@Nullable final T data) {
        return new CommonResponse<>(HttpStatus.CREATED, true, data, null);
    }

    public static <T> CommonResponse<T> fail(final ErrorCode c) {
        return new CommonResponse<>(c.getHttpStatus(), false, null, ExceptionDto.of(c));
    }
}