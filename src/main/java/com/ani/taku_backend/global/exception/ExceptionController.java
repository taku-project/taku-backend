package com.ani.taku_backend.global.exception;

import com.ani.taku_backend.global.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;

import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

@Slf4j
@RestControllerAdvice
public class ExceptionController {
    // 존재하지 않는 요청에 대한 예외
    @ExceptionHandler(value = {NoHandlerFoundException.class, HttpRequestMethodNotSupportedException.class})
    public ApiResponse<?> handleNoPageFoundException(Exception e) {
        log.error("GlobalExceptionHandler catch NoHandlerFoundException : {}", e.getMessage());
        return ApiResponse.fail(ErrorCode.NOT_FOUND_END_POINT);
    }

    // 유효성 검사 예외
    @ExceptionHandler(value = {MethodArgumentNotValidException.class , BindException.class})
    public ApiResponse<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("GlobalExceptionHandler catch MethodArgumentNotValidException : {}", e.getMessage());
        return ApiResponse.fail(ErrorCode.INVALID_INPUT_VALUE);
    }


    // 커스텀 예외
    @ExceptionHandler(value = {CustomException.class})
    public ApiResponse<?> handleCustomException(CustomException e) {
        log.error("handleCustomException() in GlobalExceptionHandler throw CustomException : {}", e.getMessage());
        return ApiResponse.fail(e.getErrorCode());
    }

    // 기본 예외
    @ExceptionHandler(value = {Exception.class})
    public ApiResponse<?> handleException(Exception e) {
        log.error("handleException() in GlobalExceptionHandler throw Exception : {}", e.getMessage());
        e.printStackTrace();
        return ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR);
    }
}