package com.ani.taku_backend.shorts.repository;

import com.ani.taku_backend.shorts.domain.entity.Shorts;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ShortsRepository extends MongoRepository<Shorts, String>, CustomShortsRepository {
}
