package com.ani.taku_backend.post.service;

import com.ani.taku_backend.post.model.dto.PostCreateRequestDTO;
import com.ani.taku_backend.post.model.dto.PostListRequestDTO;
import com.ani.taku_backend.post.model.dto.PostListResponseDTO;
import com.ani.taku_backend.post.model.dto.PostUpdateRequestDTO;
import com.ani.taku_backend.user.model.entity.User;

public interface PostService {

    PostListResponseDTO findAllPostList(PostListRequestDTO postListRequestDTO);

    Long createPost(PostCreateRequestDTO postCreateRequestDTO, User user);

    Long updatePost(Long postId, PostUpdateRequestDTO postUpdateRequestDTO, User user);

    void deletePost(Long postId, User user);
}
