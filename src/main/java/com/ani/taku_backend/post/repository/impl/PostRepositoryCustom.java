package com.ani.taku_backend.post.repository.impl;


import com.ani.taku_backend.post.model.dto.PostListRequestDTO;
import com.ani.taku_backend.post.repository.impl.dto.FindAllPostQuerydslDTO;

import java.util.List;

public interface PostRepositoryCustom {

    List<FindAllPostQuerydslDTO> findAllPostWithNoOffset(PostListRequestDTO postListRequestDTO);
}
