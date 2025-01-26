package com.ani.taku_backend.comments.repository;

import com.ani.taku_backend.comments.model.entity.Comments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface CommentsRepository extends JpaRepository<Comments, Long> {
    @Query("SELECT c FROM Comments c " +
            "JOIN FETCH c.user " +
            "WHERE c.post.id = :postId " +
            "AND c.deletedAt IS NULL " +
            "AND c.parentComment IS NULL " +
            "ORDER BY c.createdAt DESC")
    List<Comments> findParentComments(@Param("postId") Long postId);

    @Query("SELECT c FROM Comments c " +
            "JOIN FETCH c.user " +
            "WHERE c.parentComment.id = :parentId " +
            "AND c.deletedAt IS NULL " +
            "ORDER BY c.createdAt ASC")
    List<Comments> findChildComments(@Param("parentId") Long parentId);
}