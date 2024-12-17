package com.ani.taku_backend.global.response;

import com.ani.taku_backend.global.exception.CustomException;
import com.ani.taku_backend.global.exception.ErrorCode;
import com.ani.taku_backend.global.exception.ExceptionDto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;

public record ApiResponse<T>(
        @JsonIgnore
        HttpStatus httpStatus,
        boolean success,
        @Nullable T data,
        @Nullable ExceptionDto error
) {

    public static <T> ApiResponse<T> ok(@Nullable final T data) {
        return new ApiResponse<>(HttpStatus.OK, true, data, null);
    }

    public static <T> ApiResponse<T> created(@Nullable final T data) {
        return new ApiResponse<>(HttpStatus.CREATED, true, data, null);
    }

    public static <T> ApiResponse<T> fail(final ErrorCode c) {
        return new ApiResponse<>(c.getHttpStatus(), false, null, ExceptionDto.of(c));
    }
}