package com.ani.taku_backend.post.repository.impl;

import com.ani.taku_backend.common.enums.InteractionType;
import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.post.model.entity.PostInteractionCounter;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import static com.ani.taku_backend.common.exception.ErrorCode.NOT_FOUND_INTERACTION;

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
        Query query = new Query(Criteria.where("_id").is(postId));
        Update update = new Update();

        switch (type) {
            case LIKE -> update.inc("post_likes", 1);
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
        Query query = new Query(Criteria.where("_id").is(postId));
        Update update = new Update();

        switch (type) {
            case LIKE -> update.inc("post_likes", -1);
//            case DISLIKE -> update.inc("post_dislikes", -1);   // 싫어요 기능은 없지만 확장 가능성을 대비해서 미리 생성
            default -> throw new DuckwhoException(NOT_FOUND_INTERACTION);
        }

        mongoTemplate.updateFirst(query, update, PostInteractionCounter.class);
    }

    @Override
    public long getPostLikes(Long postId) {
        Query query = new Query(Criteria.where("_id").is(postId));
        query.fields().include("post_likes");   // 좋아요 수 반환

        PostInteractionCounter result = mongoTemplate.findOne(query, PostInteractionCounter.class);

        if (result == null) {   // 없으면 0
            return 0;
        }
        return result.getPostLikes();
    }
}
