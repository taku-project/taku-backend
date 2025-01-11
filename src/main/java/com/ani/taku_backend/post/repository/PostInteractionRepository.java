package com.ani.taku_backend.post.repository;

import com.ani.taku_backend.post.model.entity.PostInteraction;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PostInteractionRepository extends MongoRepository<PostInteraction, String> {
    Optional<PostInteraction> findByPostIdAndUserId(Long postId, Long userId);
}
