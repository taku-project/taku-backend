package com.ani.taku_backend.shorts.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CustomShortsRepositoryImpl implements CustomShortsRepository {
    private final MongoTemplate mongoTemplate;
}