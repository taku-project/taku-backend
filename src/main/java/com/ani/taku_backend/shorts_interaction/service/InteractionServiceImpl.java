package com.ani.taku_backend.shorts_interaction.service;

import com.ani.taku_backend.common.enums.InteractionType;
import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.exception.ErrorCode;
import com.ani.taku_backend.common.exception.FileException;
import com.ani.taku_backend.shorts.domain.entity.Interaction;
import com.ani.taku_backend.shorts.domain.entity.Shorts;
import com.ani.taku_backend.shorts.repository.ShortsRepository;
import com.ani.taku_backend.shorts_interaction.domain.dto.InteractionResponse;
import com.ani.taku_backend.shorts_interaction.repository.InteractionRepository;
import com.ani.taku_backend.user.model.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
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

        InteractionResponse userLikeInterAction = interactionRepository.findUserLikeInterAction(user.getUserId(), shortsId);

        // 좋아요가 없을 때
        if(userLikeInterAction.getLike() == null) {
            boolean hasDislike = userLikeInterAction.getDislike() == null;
            shorts.addLike(hasDislike);
            Interaction interaction = Interaction.create(shorts, user.getUserId(), InteractionType.LIKE);

            shortsRepository.save(shorts);
            if(hasDislike) {
                String disLikeId = userLikeInterAction.getDislike().getId();
                Interaction disLikeInteraction = interactionRepository.findById(new ObjectId(disLikeId))
                        .orElseThrow(() -> new DuckwhoException(ErrorCode.NOT_FOUND_INTERACTION));
                interactionRepository.delete(disLikeInteraction);
            }
            interactionRepository.save(interaction);
        }
    }

}
