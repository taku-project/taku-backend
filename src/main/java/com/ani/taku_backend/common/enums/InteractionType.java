package com.ani.taku_backend.common.enums;

/**
 * 쇼츠 상호작용 유형
 */
public enum InteractionType {
    VIEW("view"), COMMENT("comment"), LIKE("like"), DISLIKE("dislike"), SHARE("share");

    private final String value;

    InteractionType(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}