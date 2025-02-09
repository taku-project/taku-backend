package com.ani.taku_backend.category.domain.repository.impl;

import com.ani.taku_backend.category.domain.dto.AniGenreResDTO;

import java.util.List;

public interface CustomAnimationGenreRepository {
    List<AniGenreResDTO> findByGenreName(String keyword);
}
