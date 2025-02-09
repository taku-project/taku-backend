package com.ani.taku_backend.post.repository;

import com.ani.taku_backend.post.model.dto.FindPostQueryDTO;
import com.ani.taku_backend.post.model.entity.Post;
import com.ani.taku_backend.post.repository.impl.PostRepositoryCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {

    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE posts SET views = views + 1 WHERE id = :postId", nativeQuery = true)
    void incrementViewCount(@Param("postId") Long postId);

    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.communityImages ci LEFT JOIN FETCH ci.image WHERE p.id = :postId AND p.deletedAt IS NULL")
    Optional<Post> findByIdWithImages(@Param("postId") Long postId);

}
