package com.ani.taku_backend.shorts.domain.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InteractionField {
    DOCUMENT("interactions"),
    ID("_id"),
    USER_ID("user_id"),
    SHORTS_ID("shorts_id"),
    INTERACTION_TYPE("interaction_type"),
    SHORTS_TAGS("shorts_tags"),
    DETAILS("details"),
    ;
    private final String fieldName;
}