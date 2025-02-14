package com.ani.taku_backend.post.repository.impl;

import com.ani.taku_backend.common.enums.InteractionType;
import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.post.model.entity.Post;
import com.ani.taku_backend.post.model.entity.PostInteractionCounter;
import com.ani.taku_backend.post.model.enums.PopularPeriodType;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AddFieldsOperation;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationExpression;
import org.springframework.data.mongodb.core.aggregation.AggregationOperationContext;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.LimitOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.ani.taku_backend.common.exception.ErrorCode.NOT_FOUND_INTERACTION;
import static com.ani.taku_backend.post.model.enums.PostInteractionFields.*;

@Repository
public class PostInteractionCounterRepositoryCustomImpl implements PostInteractionCounterRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    public PostInteractionCounterRepositoryCustomImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * 좋아요 증가
     */
    @Override
    public void incrementPostInteractionCounter(long postId, InteractionType type) {
        Query query = new Query(Criteria.where(ID.getField()).is(postId));
        Update update = new Update();

        switch (type) {
            case LIKE -> update.inc(POST_LIKES.getField(), 1);
//            case DISLIKE -> update.inc("post_dislikes", 1);   // 싫어요 기능은 없지만 확장 가능성을 대비해서 미리 생성
            default -> throw new DuckwhoException(NOT_FOUND_INTERACTION);
        }

        mongoTemplate.updateFirst(query, update, PostInteractionCounter.class);
    }

    /**
     * 좋아요 감소
     */
    @Override
    public void decrementPostInteractionCounter(long postId, InteractionType type) {
        Query query = new Query(Criteria.where(ID.getField()).is(postId));
        Update update = new Update();

        switch (type) {
            case LIKE -> update.inc(POST_LIKES.getField(), -1);
//            case DISLIKE -> update.inc("post_dislikes", -1);   // 싫어요 기능은 없지만 확장 가능성을 대비해서 미리 생성
            default -> throw new DuckwhoException(NOT_FOUND_INTERACTION);
        }

        mongoTemplate.updateFirst(query, update, PostInteractionCounter.class);
    }

    /**
     * 좋아요 반환
     */
    @Override
    public long getPostLikes(Long postId) {
        Query query = new Query(Criteria.where(ID.getField()).is(postId));
        query.fields().include(POST_LIKES.getField());   // 좋아요 수 반환

        PostInteractionCounter result = mongoTemplate.findOne(query, PostInteractionCounter.class);

        if (result == null) {   // 없으면 0
            return 0;
        }
        return result.getPostLikes();
    }

    @Override
    public void updateDeletedAt(Post post) {
        Query query = new Query(Criteria.where(ID.getField()).is(post.getId()));
        Update update = new Update().set(DELETED_AT.getField(), post.getDeletedAt());
        mongoTemplate.updateFirst(query, update, PostInteractionCounter.class);
    }


    /**
     * 특정 postId 리스트의 좋아요 개수 조회
     */
    @Override
    public Map<Long, Long> findLikesByPostIds(List<Long> postIds) {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where(ID.getField()).in(postIds)),
                Aggregation.project(ID.getField(), POST_LIKES.getField())
        );

        AggregationResults<PostInteractionCounter> results = mongoTemplate.aggregate(
                aggregation, POSTS_INTERACTION_COUNTER.getField(), PostInteractionCounter.class
        );

        return results.getMappedResults().stream()
                .collect(Collectors.toMap(PostInteractionCounter::getPostId, PostInteractionCounter::getPostLikes));
    }

    @Override
    public List<PostInteractionCounter> findPopularPost(PopularPeriodType periodType) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = calculateStartDate(periodType);


        final String POPULARITY_SCORE = "popularityScore";

        ProjectionOperation projection = Aggregation.project(ID.getField(), VIEWS.getField(), POST_LIKES.getField(), DELETED_AT.getField());

        AddFieldsOperation addFields =
            Aggregation.addFields()
                .addField(POPULARITY_SCORE)
                .withValue(
                    new AggregationExpression() {
                        @Override
                        public Document toDocument(AggregationOperationContext context) {
                            return new Document("$add", Arrays.asList(
                                new Document("$multiply", Arrays.asList("$"+ VIEWS.getField() , 0.2)),
                                new Document("$multiply", Arrays.asList("$"+ POST_LIKES.getField() , 0.6)),
                                new Document("$multiply", Arrays.asList("$"+ COMMENTS.getField() , 1.2))
                            ));
                        }
                    }
                ).build();

        MatchOperation match = Aggregation.match(
            Criteria.where(DELETED_AT.getField()).isNull()
                .and(CREATED_AT.getField()).gte(startDate).lte(endDate)
        );

        SortOperation sort = Aggregation.sort(Sort.Direction.DESC, POPULARITY_SCORE);
        LimitOperation limit = Aggregation.limit(20);

        TypedAggregation<PostInteractionCounter> aggregation = Aggregation.newAggregation(PostInteractionCounter.class,
                match, projection, addFields, sort, limit
        );

        return mongoTemplate.aggregate(aggregation, PostInteractionCounter.class)
                .getMappedResults();
    }

    private LocalDateTime calculateStartDate(PopularPeriodType periodType) {
        LocalDateTime now = LocalDateTime.now();
        switch (periodType) {
            case WEEK:
                return now.minusWeeks(1);
            case MONTH:
                return now.minusDays(30);
            default:
                return now.minusWeeks(1);
        }
    }
    /**
     * 특정 사용자가 게시글에 좋아요를 눌렀는지 확인
     * @param postId 게시글 ID
     * @param userId 사용자 ID
     * @return 좋아요 여부
     */
    @Override
    public boolean isPostLikedByUser(Long postId, Long userId) {
        Query query = new Query(Criteria.where(ID.getField()).is(postId)
                .and("liked_user_ids").in(userId));
        return mongoTemplate.exists(query, PostInteractionCounter.class);
    }


}
