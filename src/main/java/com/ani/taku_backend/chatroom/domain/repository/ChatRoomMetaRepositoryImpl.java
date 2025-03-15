package com.ani.taku_backend.chatroom.domain.repository;

import com.ani.taku_backend.chatroom.domain.document.ChatMessage;
import com.ani.taku_backend.chatroom.domain.document.ChatRoomMetaInfo;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.Collections;
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
    public List<ChatRoomMetaInfo> findMetaInfosByChatRoomIds(List<Long> chatRoomIds) {
        if (chatRoomIds == null || chatRoomIds.isEmpty()) {
            return List.of();
        }

        Query query = new Query(Criteria.where("chatRoomId").in(chatRoomIds));
        return mongoTemplate.find(query, ChatRoomMetaInfo.class);
    }
}