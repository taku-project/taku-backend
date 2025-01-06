package com.ani.taku_backend.shorts_interaction.repository;

import com.ani.taku_backend.shorts.domain.dto.res.ShortsLikeInteractionResponseDTO;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomInteractionRepository {
    ShortsLikeInteractionResponseDTO findUserLikeInterAction(Long userId, String shortsId);
}