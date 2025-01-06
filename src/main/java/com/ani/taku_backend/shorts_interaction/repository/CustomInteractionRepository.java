package com.ani.taku_backend.shorts_interaction.repository;

import com.ani.taku_backend.shorts.domain.dto.res.ShortsLikeInteractionResponseDTO;
import com.ani.taku_backend.shorts_interaction.domain.dto.InteractionResponse;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomInteractionRepository {
    // 사용자가 누른 좋아요 / 싫어요 여부 반환
    ShortsLikeInteractionResponseDTO isUserLikeInterAction(Long userId, String shortsId);
    // 사용자가 누른 좋아요 / 싫어요 정보 반환
    InteractionResponse findUserLikeInterAction(Long userId, String shortsId);

}