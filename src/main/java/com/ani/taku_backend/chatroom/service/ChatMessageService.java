package com.ani.taku_backend.chatroom.service;

import com.ani.taku_backend.chatroom.domain.document.ChatMessage;
import com.ani.taku_backend.chatroom.domain.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.bson.Document;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 채팅 메시지 관련 비즈니스 로직을 처리하는 서비스
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;

    /**
     * 채팅방 ID 목록에 해당하는 각 채팅방의 마지막 메시지를 조회합니다.
     */
    public Map<Long, ChatMessage> getLastMessageMap(List<Long> chatRoomIds) {
        if (chatRoomIds == null || chatRoomIds.isEmpty()) {
            return new HashMap<>();
        }
        
        Map<Long, ChatMessage> lastMessageMap = new HashMap<>();
        
        try {
            // 신규 Aggregation 기반 메소드 사용
            List<Document> results = chatMessageRepository.findLastMessagesByChatRoomIdsGrouped(chatRoomIds);
            
            for (Document result : results) {
                Long chatRoomId = result.get("_id", Long.class);
                Document messageDoc = result.get("lastMessage", Document.class);
                if (chatRoomId != null && messageDoc != null) {
                    // Document를 ChatMessage로 변환 - 실제 구현은 프로젝트 구조에 맞게 조정 필요
                    ChatMessage message = convertToMessage(messageDoc);
                    lastMessageMap.put(chatRoomId, message);
                }
            }
        } catch (Exception e) {
            log.error("채팅방 마지막 메시지 조회 중 오류 발생", e);
            
            // 기존 방식으로 폴백
            List<ChatMessage> latestMessages = chatMessageRepository.findLatestMessagesByChatRoomIds(chatRoomIds);
            for (ChatMessage message : latestMessages) {
                if (!lastMessageMap.containsKey(message.getChatRoomId())) {
                    lastMessageMap.put(message.getChatRoomId(), message);
                }
            }
        }
        
        return lastMessageMap;
    }
    
    /**
     * MongoDB Document를 ChatMessage 객체로 변환합니다.
     *
     * @param doc MongoDB Document 객체
     * @return 변환된 ChatMessage 객체
     */
    private ChatMessage convertToMessage(Document doc) {
        ChatMessage message = new ChatMessage();

        message.setId(doc.getString("_id"));
        message.setChatRoomId(doc.getLong("chatRoomId"));
        message.setArticleId(doc.getLong("articleId"));
        message.setSenderId(doc.getLong("senderId"));
        message.setContent(doc.getString("content"));

        if (doc.get("sentAt") != null) {
            message.setSentAt(doc.getDate("sentAt").toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
        }

        if (doc.get("read") != null) {
            message.setRead(doc.getBoolean("read"));
        } else {
            message.setRead(false);
        }

        if (doc.getString("status") != null) {
            try {
                message.setStatus(com.ani.taku_backend.chatroom.domain.constant.ChatRoomStatus.valueOf(doc.getString("status")));
            } catch (IllegalArgumentException e) {
                message.setStatus(com.ani.taku_backend.chatroom.domain.constant.ChatRoomStatus.ACTIVE);
                log.warn("Invalid status value in message document: {}", doc.getString("status"));
            }
        }
        
        return message;
    }
} 