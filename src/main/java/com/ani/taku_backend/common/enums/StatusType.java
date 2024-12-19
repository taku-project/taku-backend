package com.ani.taku_backend.common.enums;

public enum StatusType {
    ACTIVE("ACTIVE"),       // 활성화
    INACTIVE("INACTIVE");   // 비활성화

    private final String value;

    StatusType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}