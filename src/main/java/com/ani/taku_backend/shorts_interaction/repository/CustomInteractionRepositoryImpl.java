package com.ani.taku_backend.shorts_interaction.repository;

import com.ani.taku_backend.common.enums.InteractionType;
import com.ani.taku_backend.shorts.domain.dto.res.ShortsLikeInteractionResponseDTO;
import com.ani.taku_backend.shorts.domain.entity.Interaction;
import com.ani.taku_backend.shorts.domain.entity.InteractionField;
import com.ani.taku_backend.shorts.domain.entity.ShortsField;
import com.ani.taku_backend.shorts_interaction.domain.dto.InteractionResponse;
import com.ani.taku_backend.shorts_interaction.domain.dto.UserInteractionResponse;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CustomInteractionRepositoryImpl implements CustomInteractionRepository {
    private final MongoTemplate mongoTemplate;

    @Override
    public ShortsLikeInteractionResponseDTO isUserLikeInterAction(Long userId, String shortsId) {
        if(userId == null) {
            return ShortsLikeInteractionResponseDTO.builder()
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
        return mongoTemplate.aggregate(userInteractionAggregation, Interaction.class, ShortsLikeInteractionResponseDTO.class).getUniqueMappedResult();
    }

    @Override
    public InteractionResponse findUserLikeDislikeInteractions(Long userId, String shortsId) {
        if(userId == null) {
            return null;
        }
        MatchOperation match = Aggregation.match(
                Criteria.where(InteractionField.SHORTS_ID.getFieldName()).is(new ObjectId(shortsId))
                        .and(ShortsField.USER_ID.getFieldName()).is(userId)
        );

        Document document = new Document()
                        .append(InteractionField.ID.getVariableName(), "$" + InteractionField.ID.getFieldName())
                        .append(InteractionField.USER_ID.getVariableName(), "$" + InteractionField.USER_ID.getFieldName())
                        .append(InteractionField.SHORTS_ID.getVariableName(), "$" + InteractionField.SHORTS_ID.getFieldName());

        GroupOperation group = Aggregation.group(InteractionField.ID.getFieldName())
                .addToSet(
                    ConditionalOperators
                        .when(Criteria.where(InteractionField.INTERACTION_TYPE.getFieldName())
                            .is(InteractionType.LIKE.getValue()))
                        .then(document)
                        .otherwise(new Document())
                ).as("likes")
                .addToSet(
                    ConditionalOperators
                        .when(Criteria.where(InteractionField.INTERACTION_TYPE.getFieldName())
                            .is(InteractionType.DISLIKE.getValue()))
                        .then(document)
                        .otherwise(new Document())
                )
                .as("dislikes");
        ProjectionOperation projection = Aggregation.project()
                .and(ArrayOperators.ArrayElemAt.arrayOf("likes").elementAt(0)).as("like")
                .and(ArrayOperators.ArrayElemAt.arrayOf("dislikes").elementAt(0)).as("dislike");

        Aggregation likeInteractionAggregation = Aggregation.newAggregation(match, group, projection);
        AggregationResults<Map> resultMaps = mongoTemplate.aggregate(likeInteractionAggregation, Interaction.class, Map.class);
        return InteractionResponse.fromMap(resultMaps.getUniqueMappedResult());
    }

    @Override
    public Optional<UserInteractionResponse> findUserLikeInteractions(Long userId, String shortsId) {
        if(userId == null) {
            return Optional.empty();
        }
        MatchOperation match = Aggregation.match(
                Criteria
                    .where(InteractionField.SHORTS_ID.getFieldName()).is(new ObjectId(shortsId))
                    .and(InteractionField.USER_ID.getFieldName()).is(userId)
                    .and(InteractionField.INTERACTION_TYPE.getFieldName()).is(InteractionType.LIKE.getValue())
        );

        ProjectionOperation projection = Aggregation.project()
                .and(InteractionField.ID.getFieldName()).as(InteractionField.ID.getVariableName())
                .and(InteractionField.SHORTS_ID.getFieldName()).as(InteractionField.SHORTS_ID.getVariableName())
                .and(InteractionField.USER_ID.getFieldName()).as(InteractionField.USER_ID.getVariableName());

        Aggregation aggregation = Aggregation.newAggregation(match, projection);

        AggregationResults<UserInteractionResponse> results = mongoTemplate.aggregate(
                aggregation, Interaction.class, UserInteractionResponse.class
        );

        return Optional.ofNullable(results.getUniqueMappedResult());
    }

    @Override
    public Optional<UserInteractionResponse> findUserDislikeInteractions(Long userId, String shortsId) {
        if(userId == null) {
            return Optional.empty();
        }
        MatchOperation match = Aggregation.match(
                Criteria
                        .where(InteractionField.SHORTS_ID.getFieldName()).is(new ObjectId(shortsId))
                        .and(InteractionField.USER_ID.getFieldName()).is(userId)
                        .and(InteractionField.INTERACTION_TYPE.getFieldName()).is(InteractionType.DISLIKE.getValue())
        );

        ProjectionOperation projection = Aggregation.project()
                .and(InteractionField.ID.getFieldName()).as(InteractionField.ID.getVariableName())
                .and(InteractionField.SHORTS_ID.getFieldName()).as(InteractionField.SHORTS_ID.getVariableName())
                .and(InteractionField.USER_ID.getFieldName()).as(InteractionField.USER_ID.getVariableName());

        Aggregation aggregation = Aggregation.newAggregation(match, projection);

        AggregationResults<UserInteractionResponse> results = mongoTemplate.aggregate(
                aggregation, Interaction.class, UserInteractionResponse.class
        );

        return Optional.ofNullable(results.getUniqueMappedResult());
    }
}
