package com.ani.taku_backend.chatroom.domain.repository;

import com.ani.taku_backend.chatroom.domain.document.ChatRoomMetaInfo;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChatRoomMetaRepositoryImpl implements ChatRoomMetaRepositoryCustom {

    private final MongoTemplate mongoTemplate;
    
    public ChatRoomMetaRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }
    
    @Override
    public List<ChatRoomMetaInfo> findMetaInfoWithLastMessages(List<Long> chatRoomIds) {
        if (chatRoomIds == null || chatRoomIds.isEmpty()) {
            return List.of();
        }

        Query query = new Query(Criteria.where("chatRoomId").in(chatRoomIds));
        List<ChatRoomMetaInfo> results = mongoTemplate.find(query, ChatRoomMetaInfo.class);

        return results;
    }

    @Override
    public boolean reactivateParticipant(Long chatRoomId, Long userId) {
        log.debug("채팅방 참여자 재활성화: chatRoomId={}, userId={}", chatRoomId, userId);
        
        // 1. 채팅방 메타 정보 조회
        Query query = new Query(Criteria.where("chatRoomId").is(chatRoomId));
        ChatRoomMetaInfo metaInfo = mongoTemplate.findOne(query, ChatRoomMetaInfo.class);
        
        if (metaInfo == null) {
            log.warn("채팅방 메타 정보를 찾을 수 없음: chatRoomId={}", chatRoomId);
            return false;
        }
        
        // 2. 참여자 상태 업데이트
        boolean updated = metaInfo.getParticipants().activateParticipant(userId);
        if (!updated) {
            log.warn("참여자 정보가 없거나 이미 활성화됨: chatRoomId={}, userId={}", chatRoomId, userId);
            return false;
        }
        
        // 3. 변경사항 저장
        mongoTemplate.save(metaInfo);
        log.debug("채팅방 참여자 재활성화 완료: chatRoomId={}, userId={}", chatRoomId, userId);
        
        return true;
    }

}