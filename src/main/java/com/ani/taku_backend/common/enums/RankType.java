package com.ani.taku_backend.common.enums;

public enum RankType {

    VIEW("view"),
    LIKE("like"),
    BOOKMARK("bookmark");

    private final String value;

    RankType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
