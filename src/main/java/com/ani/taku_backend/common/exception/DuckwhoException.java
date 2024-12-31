package com.ani.taku_backend.common.exception;

import lombok.Getter;

@Getter
public class DuckwhoException extends RuntimeException{
    private final ErrorCode errorCode;

    public DuckwhoException(ErrorCode  errorCode) {
        this.errorCode = errorCode;
    }

    public DuckwhoException(int  statusCode) {
        this.errorCode = ErrorCode.findByStatus(statusCode);
    }
    public String getMessage() {
        return errorCode.getMessage();
    }
}