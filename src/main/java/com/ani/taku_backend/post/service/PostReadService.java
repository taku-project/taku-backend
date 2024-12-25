package com.ani.taku_backend.post.service;

import com.ani.taku_backend.post.model.dto.PostDetailResponseDTO;
import com.ani.taku_backend.post.model.entity.Post;
import com.ani.taku_backend.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostReadService {

    private final PostRepository postRepository;

    @Transactional
    public PostDetailResponseDTO getPostDetail(Long postId, boolean canAddView, Long currentUserId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글 ID=" + postId));

        if (post.getDeletedAt() != null) {
            throw new IllegalStateException("이미 삭제된 게시글입니다.");
        }

        if (canAddView) {
            post.addViews();
        }

        boolean isOwner = false;
        if (post.getUser() != null && currentUserId != null && post.getUser().getUserId().equals(currentUserId)) {
            isOwner = true;
        }

        return new PostDetailResponseDTO(post, isOwner);
    }
}