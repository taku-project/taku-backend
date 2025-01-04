package com.ani.taku_backend.common.enums;

public enum ViewType {
    SHORTS("shorts"),
    CATEGORY("category"),
    SHOP("shop"),
    POST("post");

    private final String value;

    ViewType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
