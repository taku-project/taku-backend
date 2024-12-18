package com.ani.taku_backend.post.repository.impl;

import com.querydsl.jpa.impl.JPAQueryFactory;


public class PostRepositoryCustomImpl implements PostRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    public PostRepositoryCustomImpl(JPAQueryFactory jpaQueryFactory) {
        this.jpaQueryFactory = jpaQueryFactory;
    }

}
