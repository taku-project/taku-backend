package com.ani.taku_backend.user_jangter.domain;

import com.ani.taku_backend.common.enums.EnumCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CompleteJangterSortType implements EnumCode {
    ID("id"),
    TITLE("title"),
    CATEGORY_NAME("categoryName"),
    PRICE("price"),
    ;

    private final String value;
}