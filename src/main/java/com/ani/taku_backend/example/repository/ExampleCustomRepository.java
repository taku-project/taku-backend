package com.ani.taku_backend.example.repository;

import com.ani.taku_backend.example.model.entity.Example;

import java.util.List;

/*
    QueryDsl 사용 예시
 */
public interface ExampleCustomRepository {
    List<Example> findExamplesByTitle(String title);
}
