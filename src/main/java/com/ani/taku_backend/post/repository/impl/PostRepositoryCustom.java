package com.ani.taku_backend.post.repository.impl;


import com.ani.taku_backend.post.model.entity.Post;

import java.util.List;

public interface PostRepositoryCustom {
    List<Post> findAllPostWithNoOffset(String filter, Object lastValue, boolean isAsc, int limit, String keyword);
}
