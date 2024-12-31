package com.ani.taku_backend.common.response;

import com.ani.taku_backend.common.exception.ErrorCode;
import com.ani.taku_backend.common.exception.ExceptionDto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;

public record CommonResponse<T>(
        @JsonIgnore
        HttpStatus httpStatus,
        boolean success,
        @Nullable T data,
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