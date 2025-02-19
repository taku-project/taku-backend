package com.ani.taku_backend.post.model.enums;

import com.ani.taku_backend.common.enums.EnumCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PopularPeriodType implements EnumCode {
    TODAY("today"),
    WEEK("week"),
    MONTH("month"),
    ;

    private final String value;
}
