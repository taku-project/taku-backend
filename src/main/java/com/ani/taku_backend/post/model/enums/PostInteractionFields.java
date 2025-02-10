package com.ani.taku_backend.post.model.enums;

import lombok.Getter;

@Getter
public enum PostInteractionFields {

    ID("_id"),
    POST_LIKES("post_likes"),
    DELETED_AT("deleted_at"),
    CATEGORY_ID("category_id"),
    POSTS_INTERACTION_COUNTER("posts_interaction_counter"),
    ;

    private final String field;

    PostInteractionFields(String field) {
        this.field = field;
    }
}
