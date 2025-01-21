package com.ani.taku_backend.user.model.entity;

import com.ani.taku_backend.common.enums.EnumCode;
import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum UserStatus implements EnumCode {
    ACTIVE("ACTIVE"),
    INACTIVE("INACTIVE"),
    ;

    private final String value;

    public static UserStatus findKey(String status) {
        return Arrays.stream(UserStatus.values())
                .filter(userStatus -> userStatus.getValue().equals(status) || userStatus.name().equals(status))
                .findFirst()
                .orElseThrow(()-> new DuckwhoException(ErrorCode.INVALID_INPUT_VALUE));

    }
}
