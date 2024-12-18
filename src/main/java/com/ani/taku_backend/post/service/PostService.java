package com.ani.taku_backend.post.service;

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

    public List<Post> getPosts(String filter, Object lastValue, boolean isAsc, int limit) {
        return postRepository.findPostsWithNoOffset(filter, lastValue, isAsc, limit);
    }
}
