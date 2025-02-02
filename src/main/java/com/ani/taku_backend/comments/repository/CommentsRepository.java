package com.ani.taku_backend.comments.repository;

import com.ani.taku_backend.comments.model.entity.Comments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface CommentsRepository extends JpaRepository<Comments, Long> {
    /**
     * 게시글의 모든 댓글을 계층 구조로 조회합니다.
     * 부모 댓글과 자식 댓글, 그리고 각 댓글의 작성자 정보를 함께 조회합니다.
     */
    @Query("""
            SELECT DISTINCT c FROM Comments c
            LEFT JOIN c.parentComment p
            JOIN FETCH c.user
            WHERE c.post.id = :postId
            AND c.deletedAt IS NULL
            ORDER BY CASE WHEN c.parentComment IS NULL THEN c.createdAt ELSE p.createdAt END DESC,
                     CASE WHEN c.parentComment IS NULL THEN 0 ELSE c.createdAt END ASC
            """)
    List<Comments> findParentComments(@Param("postId") Long postId);
}