package com.ani.taku_backend.common.enums;

import com.ani.taku_backend.common.exception.DuckwhoException;

import static com.ani.taku_backend.common.exception.ErrorCode.INVALID_INPUT_VALUE;

/**
 * 쇼츠 상호작용 유형
 */
public enum InteractionType implements EnumCode {
    VIEW("view"),
    COMMENT("comment"),
    LIKE("like"),
    DISLIKE("dislike"),
    SHARE("share"),
    ;

    private final String value;

    InteractionType(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return this.value;
    }

    public static InteractionType fromValue(String value) {
        for (InteractionType type : InteractionType.values()) {
            if (type.getValue().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new DuckwhoException(INVALID_INPUT_VALUE);
    }
}
