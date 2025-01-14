package com.ani.taku_backend.post.repository.impl;

import com.ani.taku_backend.common.enums.InteractionType;

public interface PostInteractionCounterRepositoryCustom {

    void incrementPostInteractionCounter(long postId, InteractionType type);
    void decrementPostInteractionCounter(long postId, InteractionType type);

    long getPostLikes(Long postId);
}
