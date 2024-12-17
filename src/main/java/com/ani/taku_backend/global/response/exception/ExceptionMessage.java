package com.ani.taku_backend.global.response.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ExceptionMessage {
    INVALIDATE_USER(1000,"해당하는 사용자가 없습니다."),
    INVALIDATE__GOODS(1001,"해당하는 상품이 존재하지 않습니다.");

    private final int code ;
    private final String message;

}
