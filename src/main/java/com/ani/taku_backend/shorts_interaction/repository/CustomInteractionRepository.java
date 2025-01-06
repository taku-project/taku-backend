package com.ani.taku_backend.shorts_interaction.repository;

import com.ani.taku_backend.shorts.domain.dto.res.ShortsLikeInteractionResponse;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomInteractionRepository {
    ShortsLikeInteractionResponse findUserLikeInterAction(Long userId, String shortsId);
}