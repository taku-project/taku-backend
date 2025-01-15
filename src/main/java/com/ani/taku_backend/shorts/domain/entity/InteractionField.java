package com.ani.taku_backend.shorts.domain.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InteractionField {
    DOCUMENT("interactions", "interaction"),
    ID("_id", "id"),
    USER_ID("user_id", "userId"),
    SHORTS_ID("shorts_id", "shortsId"),
    INTERACTION_TYPE("interaction_type", "interactionType"),
    SHORTS_TAGS("shorts_tags", "shortsTags"),
    DETAILS("details", "details"),
    CREATED_AT("created_at", "createdAt"),
    ;
    private final String fieldName;
    private final String variableName;
}