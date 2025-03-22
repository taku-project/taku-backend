package com.ani.taku_backend.admin.category.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CategorySearchType {
    ID,
    USERNAME,
    CATEGORY_NAME,
    ;
}
