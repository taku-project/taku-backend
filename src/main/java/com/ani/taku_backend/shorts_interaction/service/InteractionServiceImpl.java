package com.ani.taku_backend.shorts_interaction.service;

import com.ani.taku_backend.common.enums.InteractionType;
import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.exception.ErrorCode;
import com.ani.taku_backend.common.exception.FileException;
import com.ani.taku_backend.shorts.domain.entity.Interaction;
import com.ani.taku_backend.shorts.domain.entity.Shorts;
import com.ani.taku_backend.shorts.domain.vo.ViewDetail;
import com.ani.taku_backend.shorts.repository.ShortsRepository;
import com.ani.taku_backend.shorts_interaction.domain.dto.CreateShortsViewDTO;
import com.ani.taku_backend.shorts_interaction.domain.dto.InteractionResponse;
import com.ani.taku_backend.shorts_interaction.domain.dto.UserInteractionResponse;
import com.ani.taku_backend.shorts_interaction.repository.InteractionRepository;
import com.ani.taku_backend.user.model.entity.User;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

        Optional<InteractionResponse> userLikeInterAction = interactionRepository.findUserLikeDislikeInteractions(user.getUserId(), shortsId);

        if(userLikeInterAction.isPresent()) {
            InteractionResponse interactionResponse = userLikeInterAction.get();

            if(interactionResponse.getLike() != null) return;

            boolean hasDislike = interactionResponse.getDislike() != null;

            shorts.addLikeCount(hasDislike);

            if(hasDislike) {
                String disLikeId = interactionResponse.getDislike().getId();
                Interaction disLikeInteraction = interactionRepository.findById(new ObjectId(disLikeId))
                        .orElseThrow(() -> new DuckwhoException(ErrorCode.NOT_FOUND_INTERACTION));
                interactionRepository.delete(disLikeInteraction);
            }
        } else {
            shorts.addLikeCount();
        }

        Interaction interaction = Interaction.createLikeDisLike(shorts, user.getUserId(), InteractionType.LIKE);

        interactionRepository.save(interaction);
        shortsRepository.save(shorts);
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

        Optional<InteractionResponse> userLikeInterAction = interactionRepository.findUserLikeDislikeInteractions(user.getUserId(), shortsId);

        if(userLikeInterAction.isPresent()) {
            InteractionResponse interactionResponse = userLikeInterAction.get();

            if(interactionResponse.getDislike() != null) return;

            boolean hasLike = interactionResponse.getLike() != null;

            shorts.addDislikeCount(hasLike);

            if(hasLike) {
                String likeId = interactionResponse.getLike().getId();
                Interaction likeInteraction = interactionRepository.findById(new ObjectId(likeId))
                        .orElseThrow(() -> new DuckwhoException(ErrorCode.NOT_FOUND_INTERACTION));
                interactionRepository.delete(likeInteraction);
            }
        } else {
            shorts.addDislikeCount();
        }

        Interaction interaction = Interaction.createLikeDisLike(shorts, user.getUserId(), InteractionType.DISLIKE);

        interactionRepository.save(interaction);
        shortsRepository.save(shorts);
    }

    @Transactional
    @Override
    public void cancelDislike(User user, String shortsId) {
        Shorts shorts = shortsRepository.findById(shortsId)
                .orElseThrow(FileException.FileNotFoundException::new);

        Optional<UserInteractionResponse> userDislikeInterActionOptional = interactionRepository.findUserDislikeInteractions(user.getUserId(), shortsId);

        if(userDislikeInterActionOptional.isPresent()) {
            UserInteractionResponse interactionResponse = userDislikeInterActionOptional.get();

            shorts.addDislikeCount();
            shortsRepository.save(shorts);
            interactionRepository.deleteById(new ObjectId(interactionResponse.getId()));
        }
    }

    @Transactional
    @Override
    public void createView(CreateShortsViewDTO createShortsViewReqDTO) {
        Shorts shorts = shortsRepository.findById(createShortsViewReqDTO.getShortsId())
                .orElseThrow(FileException.FileNotFoundException::new);

        BigDecimal playTime = BigDecimal.valueOf(createShortsViewReqDTO.getPlayDuration().toNanos());
        BigDecimal viewTime = BigDecimal.valueOf(createShortsViewReqDTO.getViewDuration().toNanos());
        BigDecimal viewRatio = viewTime.divide(playTime, 4, RoundingMode.HALF_UP);

        ViewDetail detail = ViewDetail.builder()
                .playDuration(createShortsViewReqDTO.getPlayDuration())
                .viewDuration(createShortsViewReqDTO.getViewDuration())
                .viewRatio(viewRatio.doubleValue())
                .build();

        Interaction interaction = Interaction.createView(shorts,createShortsViewReqDTO.getUser().getUserId(), detail);
        interactionRepository.save(interaction);

    }

}
