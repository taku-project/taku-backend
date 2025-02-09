package com.ani.taku_backend.category.domain.repository;

import com.ani.taku_backend.category.domain.entity.AnimationGenre;
import com.ani.taku_backend.category.domain.repository.impl.CustomAnimationGenreRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnimationGenreRepository extends JpaRepository<AnimationGenre, Long>, CustomAnimationGenreRepository {

}
