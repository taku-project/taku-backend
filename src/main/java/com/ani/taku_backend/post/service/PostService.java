package com.ani.taku_backend.post.service;

import com.ani.taku_backend.post.model.dto.PopularPostLiestRequestDTO;
import com.ani.taku_backend.post.model.dto.PostCreateRequestDTO;
import com.ani.taku_backend.post.model.dto.PostDetailResponseDTO;
import com.ani.taku_backend.post.model.dto.PostListRequestDTO;
import com.ani.taku_backend.post.model.dto.PostListResponseDTO;
import com.ani.taku_backend.post.model.dto.PostUpdateRequestDTO;
import com.ani.taku_backend.post.model.enums.PopularPeriodType;
import com.ani.taku_backend.user.model.entity.User;
import org.springframework.data.domain.Pageable;

public interface PostService {

    PostListResponseDTO findPostList(PostListRequestDTO postListRequestDTO, Pageable pageable);

    Long createPost(PostCreateRequestDTO postCreateRequestDTO, User user);

    Long updatePost(Long postId, PostUpdateRequestDTO postUpdateRequestDTO, User user);

    void deletePost(Long postId, User user);

    PostDetailResponseDTO getPostDetail(Long postId, boolean canAddView, Long currentUserId);

    PopularPostLiestRequestDTO getPopularityPosts(PopularPeriodType periodType);
  
    PostDetailResponseDTO getPostDetail(Long postId, Long currentUserId);
}
