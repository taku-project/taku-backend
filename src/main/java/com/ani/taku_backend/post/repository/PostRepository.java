package com.ani.taku_backend.post.repository;

import com.ani.taku_backend.post.model.entity.Post;
import com.ani.taku_backend.post.repository.impl.PostRepositoryCustom;
import com.ani.taku_backend.user.model.entity.User;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {
    Optional<Post> findPostByUserId(Long userId);
}
