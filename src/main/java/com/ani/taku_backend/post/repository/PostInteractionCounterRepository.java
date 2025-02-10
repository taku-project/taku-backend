package com.ani.taku_backend.post.repository;

import com.ani.taku_backend.post.model.entity.PostInteractionCounter;
import com.ani.taku_backend.post.repository.impl.PostInteractionCounterRepositoryCustom;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Map;

public interface PostInteractionCounterRepository extends MongoRepository<PostInteractionCounter, Long>,
                                                            PostInteractionCounterRepositoryCustom {

}
