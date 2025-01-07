package com.ani.taku_backend.shorts_interaction.repository;

import com.ani.taku_backend.shorts.domain.dto.res.ShortsLikeInteractionResponseDTO;
import com.ani.taku_backend.shorts_interaction.domain.dto.InteractionResponse;
import com.ani.taku_backend.shorts_interaction.domain.dto.UserInteractionResponse;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomInteractionRepository {
    // 사용자가 누른 좋아요 / 싫어요 여부 반환
    ShortsLikeInteractionResponseDTO isUserLikeInterAction(Long userId, String shortsId);
    // 사용자가 누른 좋아요 / 싫어요 정보 반환
    InteractionResponse findUserLikeDislikeInteractions(Long userId, String shortsId);

    Optional<UserInteractionResponse> findUserLikeInteractions(Long userId, String shortsId);

}