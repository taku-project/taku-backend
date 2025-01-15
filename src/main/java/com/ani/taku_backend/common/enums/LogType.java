package com.ani.taku_backend.common.enums;

public enum LogType {
    SEARCH("search"), VIEW("view");

    private final String value;

    LogType(String value) {
        this.value = value;
    }
}
