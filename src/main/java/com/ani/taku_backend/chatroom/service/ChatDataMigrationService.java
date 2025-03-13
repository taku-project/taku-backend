package com.ani.taku_backend.chatroom.service;

import com.ani.taku_backend.chatroom.domain.document.ChatMessage;
import com.ani.taku_backend.chatroom.domain.document.ChatRoomMetaInfo;
import com.ani.taku_backend.chatroom.domain.repository.ChatRoomMetaRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 채팅 메시지 데이터를 ChatMessage 컬렉션에서 ChatRoomMetaInfo로 마이그레이션하는 서비스
 * 서버 시작 시 자동으로 실행됩니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatDataMigrationService {

    private final MongoTemplate mongoTemplate;
    private final ChatRoomMetaRepository chatRoomMetaRepository;

    // 한 번에 처리할 최대 메시지 수
    private static final int BATCH_SIZE = 150;

    /**
     * 서버 시작 시 자동으로 모든 채팅방의 메시지를 마이그레이션합니다.
     */
    @PostConstruct
    public void migrateOnStartup() {
        log.info("Starting automatic migration of chat messages on startup");

        try {
            int count = migrateAllChatRooms();
            log.info("Automatic migration completed: {} chat rooms processed", count);
        } catch (Exception e) {
            log.error("Error during automatic migration: {}", e.getMessage(), e);
        }
    }

    /**
     * 모든 채팅방의 메시지를 마이그레이션합니다.
     *
     * @return 처리된 채팅방 수
     */
    private int migrateAllChatRooms() {
        // 모든 ChatRoomMetaInfo 조회
        List<ChatRoomMetaInfo> metaInfos = chatRoomMetaRepository.findAll();
        log.info("Found {} chat rooms to process", metaInfos.size());

        int successCount = 0;

        for (ChatRoomMetaInfo metaInfo : metaInfos) {
            try {
                boolean success = migrateSingleChatRoom(metaInfo.getChatRoomId());
                if (success) {
                    successCount++;
                }
            } catch (Exception e) {
                log.error("Error migrating room {}: {}", metaInfo.getChatRoomId(), e.getMessage());
            }
        }

        return successCount;
    }

    /**
     * 특정 채팅방의 메시지를 마이그레이션합니다.
     *
     * @param chatRoomId 채팅방 ID
     * @return 성공 여부
     */
    private boolean migrateSingleChatRoom(Long chatRoomId) {
        try {
            // 채팅방 메타정보 조회
            ChatRoomMetaInfo metaInfo = chatRoomMetaRepository.findByChatRoomId(chatRoomId)
                    .orElseThrow(() -> new IllegalArgumentException("채팅방 메타정보가 존재하지 않습니다"));

            // 이미 메시지가 있는지 확인
            if (metaInfo.getMessages() != null && !metaInfo.getMessages().isEmpty()) {
                return true; // 이미 마이그레이션됨
            }

            // 채팅방의 모든 메시지 조회 (시간순 정렬)
            Query query = new Query(Criteria.where("chatRoomId").is(chatRoomId))
                    .with(Sort.by(Sort.Order.asc("sentAt")));

            List<ChatMessage> allMessages = mongoTemplate.find(query, ChatMessage.class);

            if (allMessages.isEmpty()) {
                return true; // 메시지가 없음
            }

            // 메시지가 많은 경우 최신 메시지만 가져옴
            List<ChatMessage> messages = new ArrayList<>();
            int startIndex = Math.max(0, allMessages.size() - BATCH_SIZE);

            for (int i = startIndex; i < allMessages.size(); i++) {
                messages.add(allMessages.get(i));
            }

            // 메타정보 업데이트
            metaInfo.setMessages(messages);
            metaInfo.setMessageCount(allMessages.size()); // 전체 메시지 수는 정확하게 설정

            // 마지막 메시지 ID와 업데이트 시간 설정
            if (!messages.isEmpty()) {
                ChatMessage lastMessage = messages.get(messages.size() - 1);
                metaInfo.setLastMessageId(lastMessage.getId());
                metaInfo.setUpdateAt(Instant.now());
            }

            // 저장
            chatRoomMetaRepository.save(metaInfo);

            return true;

        } catch (Exception e) {
            log.error("Error migrating chat room {}: {}", chatRoomId, e.getMessage());
            return false;
        }
    }
}