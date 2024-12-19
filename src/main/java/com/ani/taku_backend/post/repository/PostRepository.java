package com.ani.taku_backend.post.repository;

import com.ani.taku_backend.post.model.entity.Post;
import com.ani.taku_backend.post.repository.impl.PostRepositoryCustom;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {
}
