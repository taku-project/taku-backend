package com.ani.taku_backend.chatroom.domain.repository;

import com.ani.taku_backend.chatroom.domain.document.ChatMessage;
import com.ani.taku_backend.chatroom.domain.document.ChatRoomMetaInfo;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public Map<Long, Integer> getUnreadCountMap(List<Long> chatRoomIds, Long userId) {
        log.info("MongoDB 안 읽은 메시지 수 조회 시작: chatRoomIds={}, userId={}", chatRoomIds, userId);
        if (chatRoomIds == null || chatRoomIds.isEmpty()) {
            log.warn("채팅방 ID 목록이 비어있습니다");
            return Map.of();
        }
        
        // 먼저 채팅방 메타 정보 가져오기
        Query query = new Query(Criteria.where("chatRoomId").in(chatRoomIds));
        List<ChatRoomMetaInfo> metaInfos = mongoTemplate.find(query, ChatRoomMetaInfo.class);
        
        // 최적화: 채팅방별로 messageStock 값을 효율적으로 추출
        Map<Long, Integer> unreadCountMap = new HashMap<>();
        
        for (ChatRoomMetaInfo metaInfo : metaInfos) {
            Integer unreadCount = 0;
            if (metaInfo.getParticipants() != null && 
                metaInfo.getParticipants().getInfo() != null && 
                metaInfo.getParticipants().getInfo().containsKey(userId)) {
                
                unreadCount = metaInfo.getParticipants().getInfo().get(userId).getMessageStock();
            }
            unreadCountMap.put(metaInfo.getChatRoomId(), unreadCount);
        }
        
        // 모든 채팅방 ID에 대해 결과가 있는지 확인하고 없으면 0 추가
        for (Long chatRoomId : chatRoomIds) {
            unreadCountMap.putIfAbsent(chatRoomId, 0);
        }
        
        log.info("안 읽은 메시지 수 맵 생성 완료: {} 개의 채팅방, 총 {} 개의 읽지 않은 메시지", 
            unreadCountMap.size(), 
            unreadCountMap.values().stream().mapToInt(Integer::intValue).sum());
        
        return unreadCountMap;
    }

    @Override
    public Map<Long, ChatMessage> getLastMessageMap(List<Long> chatRoomIds) {
        if (chatRoomIds == null || chatRoomIds.isEmpty()) {
            return new HashMap<>();
        }

        Query query = new Query(Criteria.where("chatRoomId").in(chatRoomIds));
        List<ChatRoomMetaInfo> metaInfos = mongoTemplate.find(query, ChatRoomMetaInfo.class);

        Map<Long, ChatMessage> lastMessageMap = new HashMap<>();

        for (ChatRoomMetaInfo metaInfo : metaInfos) {
            List<ChatMessage> messages = metaInfo.getMessages();
            if (messages != null && !messages.isEmpty()) {
                // 마지막 메시지 가져오기
                ChatMessage lastMessage = messages.get(messages.size() - 1);
                lastMessageMap.put(metaInfo.getChatRoomId(), lastMessage);
            }
        }

        return lastMessageMap;
    }
}