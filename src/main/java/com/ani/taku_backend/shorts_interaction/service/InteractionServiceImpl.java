package com.ani.taku_backend.shorts_interaction.service;

import com.ani.taku_backend.common.enums.InteractionType;
import com.ani.taku_backend.common.exception.FileException;
import com.ani.taku_backend.shorts.domain.dto.res.ShortsLikeInteractionResponse;
import com.ani.taku_backend.shorts.domain.dto.res.ShortsLikeInteractionResponseDTO;
import com.ani.taku_backend.shorts.domain.entity.Interaction;
import com.ani.taku_backend.shorts.domain.entity.Shorts;
import com.ani.taku_backend.shorts.repository.ShortsRepository;
import com.ani.taku_backend.shorts_interaction.repository.InteractionRepository;
import com.ani.taku_backend.user.model.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InteractionServiceImpl implements InteractionService {
    private final InteractionRepository interactionRepository;
    private final ShortsRepository shortsRepository;

    @Transactional
    @Override
    public void addLike(User user, String shortsId) {
        Shorts shorts = shortsRepository.findById(shortsId)
                .orElseThrow(FileException.FileNotFoundException::new);

        ShortsLikeInteractionResponseDTO likeInteractionResponse = interactionRepository.findUserLikeInterAction(user.getUserId(), shortsId);

        // 좋아요가 없을 때
        if(!likeInteractionResponse.isUserLike()) {
            shorts.addLike(likeInteractionResponse.isUserDislike());
            Interaction interaction = Interaction.create(shorts, user.getUserId(), InteractionType.LIKE, null);

            shortsRepository.save(shorts);
            interactionRepository.save(interaction);
        }
    }

}
