package com.ani.taku_backend.admin.category.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CategoryLogType {
    CREATE,
    UPDATE,
    DELETE;
}
