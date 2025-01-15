package com.ani.taku_backend.category.domain.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CategoryStatus {
    ACTIVE,
    INACTIVE,
    PENDING,
    ;
    private String name;
}