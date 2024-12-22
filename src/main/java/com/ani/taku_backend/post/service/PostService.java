package com.ani.taku_backend.post.service;

import com.ani.taku_backend.post.model.dto.PostListResponseDTO;
import com.ani.taku_backend.post.model.entity.Post;
import com.ani.taku_backend.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    public List<PostListResponseDTO> findAllPost(String filter, Long lastValue, boolean isAsc, int limit, String keyword, Long categoryId) {

        /**
         * 검증 로직
         * - 공백만 있는 keyword null 처리
         * - 공백 제거(양옆, 중간)
         */
        if (keyword != null) {
            keyword = keyword.trim().isEmpty() ? null : keyword.replaceAll("\\s+", "");
        }
        List<Post> allPost = postRepository.findAllPostWithNoOffset(filter, lastValue, isAsc, limit, keyword, categoryId);
        return allPost.stream().map(PostListResponseDTO::new).toList();
    }
}
