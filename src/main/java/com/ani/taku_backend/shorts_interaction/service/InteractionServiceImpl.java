package com.ani.taku_backend.shorts_interaction.service;

import com.ani.taku_backend.common.enums.InteractionType;
import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.exception.ErrorCode;
import com.ani.taku_backend.common.exception.FileException;
import com.ani.taku_backend.shorts.domain.entity.Interaction;
import com.ani.taku_backend.shorts.domain.entity.Shorts;
import com.ani.taku_backend.shorts.repository.ShortsRepository;
import com.ani.taku_backend.shorts_interaction.domain.dto.InteractionResponse;
import com.ani.taku_backend.shorts_interaction.domain.dto.UserInteractionResponse;
import com.ani.taku_backend.shorts_interaction.repository.InteractionRepository;
import com.ani.taku_backend.user.model.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.Optional;

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

        InteractionResponse userLikeInterAction = interactionRepository.findUserLikeDislikeInteractions(user.getUserId(), shortsId);

        // 좋아요가 없을 때
        if(userLikeInterAction.getLike() == null) {
            boolean hasDislike = userLikeInterAction.getDislike() != null;
            shorts.addLikeCount(hasDislike);
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

    @Transactional
    @Override
    public void cancelLike(User user, String shortsId) {
        Shorts shorts = shortsRepository.findById(shortsId)
                .orElseThrow(FileException.FileNotFoundException::new);

        Optional<UserInteractionResponse> userLikeInterActionOptional = interactionRepository.findUserLikeInteractions(user.getUserId(), shortsId);

        if(userLikeInterActionOptional.isPresent()) {
            UserInteractionResponse interactionResponse = userLikeInterActionOptional.get();
            shorts.decreaseLikeCount();
            shortsRepository.save(shorts);
            interactionRepository.deleteById(new ObjectId(interactionResponse.getId()));
        }
    }

    @Transactional
    @Override
    public void addDislike(User user, String shortsId) {
        Shorts shorts = shortsRepository.findById(shortsId)
                            .orElseThrow(FileException.FileNotFoundException::new);

        InteractionResponse userLikeInterAction = interactionRepository.findUserLikeDislikeInteractions(user.getUserId(), shortsId);

        // 싫어요가 없을 때만 추가
        if(userLikeInterAction.getDislike() == null) {
            boolean hasLike = userLikeInterAction.getLike() != null;
            shorts.addDislikeCount(hasLike);
            Interaction dislikeInteraction = Interaction.create(shorts, user.getUserId(), InteractionType.DISLIKE);

            shortsRepository.save(shorts);
            if(hasLike) {
                String likeId = userLikeInterAction.getLike().getId();
                Interaction likeInteraction = interactionRepository.findById(new ObjectId(likeId))
                        .orElseThrow(() -> new DuckwhoException(ErrorCode.NOT_FOUND_INTERACTION));
                interactionRepository.delete(likeInteraction);
            }
            interactionRepository.save(dislikeInteraction);
        }
    }

    @Transactional
    @Override
    public void cancelDislike(User user, String shortsId) {
        Shorts shorts = shortsRepository.findById(shortsId)
                .orElseThrow(FileException.FileNotFoundException::new);

        Optional<UserInteractionResponse> userDislikeInterActionOptional = interactionRepository.findUserDislikeInteractions(user.getUserId(), shortsId);

        if(userDislikeInterActionOptional.isPresent()) {
            UserInteractionResponse interactionResponse = userDislikeInterActionOptional.get();
            shorts.decreaseDislikeCount();
            shortsRepository.save(shorts);
            interactionRepository.deleteById(new ObjectId(interactionResponse.getId()));
        }
    }

}
