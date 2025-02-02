package com.ani.taku_backend.comments.repository;

import com.ani.taku_backend.comments.model.entity.Comments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface CommentsRepository extends JpaRepository<Comments, Long> {
    /**
     * 게시글의 모든 부모 댓글과 그에 딸린 자식 댓글(대댓글)을 한 번에 조회합니다.
     * 댓글 작성자 정보도 함께 조회하여 N+1 문제를 방지합니다.
     *
     * @param postId 조회할 게시글의 ID
     * @return 삭제되지 않은 모든 부모 댓글과 자식 댓글 목록 (최신순 정렬)
     */
    @Query("""
            SELECT DISTINCT c FROM Comments c
            LEFT JOIN Comments cc ON cc.parentComment = c
            JOIN FETCH c.user
            LEFT JOIN FETCH cc.user
            WHERE c.post.id = :postId
            AND c.deletedAt IS NULL
            AND c.parentComment IS NULL
            ORDER BY c.createdAt DESC
            """)
    List<Comments> findParentComments(@Param("postId") Long postId);
}