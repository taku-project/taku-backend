package com.ani.taku_backend.post.repository.impl;


import com.ani.taku_backend.post.model.dto.FindPostQueryDTO;
import com.ani.taku_backend.post.model.dto.PopularPostItemDTO;
import com.ani.taku_backend.post.model.dto.PostListRequestDTO;
import com.ani.taku_backend.post.model.entity.PostInteractionCounter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;


public interface PostRepositoryCustom {

    Page<FindPostQueryDTO> findPostListPage(PostListRequestDTO postListRequestDTO, Pageable pageable);

    List<PopularPostItemDTO> findPopularityPosts(List<Long> postId);
}
