package com.ani.taku_backend.common.enums;

import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public interface EnumCode {
    @JsonValue
    String getValue();

    @JsonCreator
    static <E extends Enum<E> & EnumCode> E fromValue(Class<E> enumType, String value) {
        for (E enumConstant : enumType.getEnumConstants()) {
            if (enumConstant.name().equalsIgnoreCase(value)) {
                return enumConstant;
            }
        }
        throw new DuckwhoException(ErrorCode.INVALID_INPUT_VALUE);
    }
}
