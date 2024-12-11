package com.ani.taku_backend.example.repository;

import com.ani.taku_backend.example.model.entity.Example;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.ani.taku_backend.example.model.entity.QExample.example;

@RequiredArgsConstructor
public class ExampleCustomRepositoryImpl implements ExampleCustomRepository {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<Example> findExamplesByTitle(String title) {
        return jpaQueryFactory.selectFrom(example)
                .where(
                        example.exampleTitle.like("%" + title + "%")
                )
                .orderBy(example.exampleId.asc())
                .fetch();
    }
//    테스트
}
