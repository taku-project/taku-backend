package com.ani.taku_backend.post.repository.impl;


import com.ani.taku_backend.post.model.dto.FindPostQueryDTO;
import com.ani.taku_backend.post.model.dto.PostListRequestDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface PostRepositoryCustom {

    Page<FindPostQueryDTO> findPostListPage(PostListRequestDTO postListRequestDTO, Pageable pageable);
}
