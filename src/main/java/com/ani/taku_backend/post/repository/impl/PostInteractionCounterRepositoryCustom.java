package com.ani.taku_backend.post.repository.impl;

import com.ani.taku_backend.common.enums.InteractionType;
import com.ani.taku_backend.post.model.entity.Post;
import com.ani.taku_backend.post.model.entity.PostInteractionCounter;
import com.ani.taku_backend.post.model.enums.PopularPeriodType;

import java.util.List;
import java.util.Map;

public interface PostInteractionCounterRepositoryCustom {

    void incrementPostInteractionCounter(long postId, InteractionType type);

    void decrementPostInteractionCounter(long postId, InteractionType type);

    long getPostLikes(Long postId);

    void updateDeletedAt(Post post);

    Map<Long, Long> findLikesByPostIds(List<Long> postIds);

    List<PostInteractionCounter> findPopularPost(PopularPeriodType periodType);
}
