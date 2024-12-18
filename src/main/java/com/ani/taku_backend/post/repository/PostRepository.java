package com.ani.taku_backend.post.repository;

import com.ani.taku_backend.post.model.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}
