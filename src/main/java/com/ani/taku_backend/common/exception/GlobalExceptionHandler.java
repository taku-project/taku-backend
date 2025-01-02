package com.ani.taku_backend.common.exception;

import com.ani.taku_backend.common.response.CommonResponse;
import lombok.extern.slf4j.Slf4j;

import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({NoHandlerFoundException.class, HttpRequestMethodNotSupportedException.class})
    public CommonResponse handleNoPageFoundException(Exception e) {
        log.error("GlobalExceptionHandler catch NoHandlerFoundException : {}", e.getMessage());
        return CommonResponse.fail(ErrorCode.NOT_FOUND_END_POINT);
    }

    @ExceptionHandler(DuckwhoException.class)
    public CommonResponse handleDuckwhoException(DuckwhoException e) {
        log.error("handleDuckwhoException() in GlobalExceptionHandler throw DuckwhoException : {}", e.getMessage());
        return CommonResponse.fail(e.getErrorCode());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ApiResponse handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        log.error("handleMaxUploadSizeExceededException() in MaxUploadSizeExceededException : {}", e.getMessage());

        return ApiResponse.fail(ErrorCode.FILE_SIZE_EXCEED);
    }

    @ExceptionHandler(Exception.class)
    public CommonResponse handleException(Exception e) {
        log.error("handleException() in GlobalExceptionHandler throw Exception : {}", e.getMessage());
        e.printStackTrace();
        return CommonResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR);
    }

        // 유효성 검사 예외
    @ExceptionHandler(value = {MethodArgumentNotValidException.class , BindException.class})
    public CommonResponse<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("GlobalExceptionHandler catch MethodArgumentNotValidException : {}", e.getMessage());
        return CommonResponse.fail(ErrorCode.INVALID_INPUT_VALUE);
    }


}