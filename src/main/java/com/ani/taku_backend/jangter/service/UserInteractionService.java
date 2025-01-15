package com.ani.taku_backend.jangter.service;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.ani.taku_backend.common.annotation.RequireUser;
import com.ani.taku_backend.common.enums.LogType;
import com.ani.taku_backend.jangter.model.entity.UserInteraction;
import com.ani.taku_backend.user.model.dto.PrincipalUser;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserInteractionService {

    private final MongoTemplate mongoTemplate;

    /**
     * 사용자 상호작용 로그 저장
     * @param <T> 로그 상세 정보 타입
     * @param userId 사용자 ID
     * @param logType 로그 유형
     * @param logDetail 로그 상세 정보
     */
    @RequireUser(allowAnonymous = true)
    public <T extends UserInteraction.LogDetail> void saveLog(PrincipalUser principalUser, LogType logType, T logDetail) {

        if(principalUser.getUser() == null) {
            return;
        }

        UserInteraction<T> interaction = UserInteraction.<T>builder()
            .userId(principalUser.getUserId())
            .logType(logType)
            .logDetail(logDetail)
            .createdAt(LocalDateTime.now())
            .build();

        try{
            mongoTemplate.save(interaction);
        } catch (Exception e) {
            log.error("Failed to save log : {}", e.getMessage());
        }
    }

    public List<UserInteraction> findLatestByUserId(Long userId , LogType logType) {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        Query query = Query.query(
            Criteria.where("user_id").is(userId)
            .and("interaction_type").is(logType)
            .and("created_at").gte(sevenDaysAgo)
        )
        .with(Sort.by(Sort.Direction.DESC, "created_at"));

        return mongoTemplate.find(query, UserInteraction.class);
    }
}

