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
        if (chatRoomIds == null || chatRoomIds.isEmpty()) {
            return Map.of();
        }

        Query query = new Query(Criteria.where("chatRoomId").in(chatRoomIds));
        List<ChatRoomMetaInfo> metaInfos = mongoTemplate.find(query, ChatRoomMetaInfo.class);

        Map<Long, Integer> unreadCountMap = new HashMap<>();
        for (ChatRoomMetaInfo metaInfo : metaInfos) {
            Integer unreadCount = metaInfo.getUnreadCount(userId);
            unreadCountMap.put(metaInfo.getChatRoomId(), unreadCount);
        }

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