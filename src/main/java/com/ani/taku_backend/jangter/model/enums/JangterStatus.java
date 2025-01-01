package com.ani.taku_backend.jangter.model.enums;

public enum JangterStatus {
    ON_SALE("판매중"),
    SOLD("판매완료"),
    RESERVED("예약중");

    private final String description;

    JangterStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}