package com.ani.taku_backend.category.domain.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CategoryOrderType {
    ID,
    NAME,
    CREATED_AT,
    MODIFIED_AT,
    ;
}
