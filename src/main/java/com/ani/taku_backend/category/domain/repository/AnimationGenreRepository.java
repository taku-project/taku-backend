package com.ani.taku_backend.category.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ani.taku_backend.category.domain.entity.AnimationGenre;

public interface AnimationGenreRepository extends JpaRepository<AnimationGenre, Long> {

}
