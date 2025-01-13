package com.ani.taku_backend.post.repository;

import com.ani.taku_backend.post.model.entity.Post;
import com.ani.taku_backend.post.repository.impl.PostRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {

    long countPostByDeletedAtIsNullAndCategoryId(long categoryId);

}
