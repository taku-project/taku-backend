package com.ani.taku_backend.comments.repository;

import com.ani.taku_backend.comments.model.entity.Comments;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentsRepository extends JpaRepository<Comments, Long> {
}
