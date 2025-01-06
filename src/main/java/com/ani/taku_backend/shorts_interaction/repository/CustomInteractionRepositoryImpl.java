package com.ani.taku_backend.shorts_interaction.repository;

import com.ani.taku_backend.common.enums.InteractionType;
import com.ani.taku_backend.shorts.domain.dto.res.ShortsLikeInteractionResponse;
import com.ani.taku_backend.shorts.domain.entity.Interaction;
import com.ani.taku_backend.shorts.domain.entity.InteractionField;
import com.ani.taku_backend.shorts.domain.entity.ShortsField;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CustomInteractionRepositoryImpl implements CustomInteractionRepository {
    private final MongoTemplate mongoTemplate;

    @Override
    public ShortsLikeInteractionResponse findUserLikeInterAction(Long userId, String shortsId) {
        if(userId == null) {
            return ShortsLikeInteractionResponse.builder()
                    .userLike(false)
                    .userDislike(false)
                    .build();
        }

        MatchOperation match = Aggregation.match(
                Criteria.where(InteractionField.SHORTS_ID.getFieldName()).is(new ObjectId(shortsId))
                    .and(ShortsField.USER_ID.getFieldName()).is(userId)
        );

        GroupOperation group = Aggregation.group(ShortsField.ID.getFieldName())
                .max(
                    ConditionalOperators.when(
                        Criteria.where(InteractionField.INTERACTION_TYPE.getFieldName())
                            .is(InteractionType.LIKE.getValue())
                    )
                    .then(true)
                    .otherwise(false)
                ).as("userLike")
                .max(
                    ConditionalOperators.when(
                        Criteria.where(InteractionField.INTERACTION_TYPE.getFieldName())
                                .is(InteractionType.DISLIKE.getValue())
                    )
                    .then(true)
                    .otherwise(false)
                ).as("userDislike")
                ;

        Aggregation userInteractionAggregation = Aggregation.newAggregation(match, group);
        return mongoTemplate.aggregate(userInteractionAggregation, Interaction.class, ShortsLikeInteractionResponse.class).getUniqueMappedResult();
    }
}
