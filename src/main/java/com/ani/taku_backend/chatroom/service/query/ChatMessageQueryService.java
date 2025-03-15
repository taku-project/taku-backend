package com.ani.taku_backend.chatroom.service.query;

import com.ani.taku_backend.chatroom.domain.document.ChatRoomMetaInfo;
import com.ani.taku_backend.chatroom.domain.document.ChatMessage;
import com.ani.taku_backend.chatroom.domain.dto.response.ChatMessageListResponseDTO;
import com.ani.taku_backend.chatroom.domain.entity.ChatRoom;
import com.ani.taku_backend.chatroom.domain.repository.ChatRoomMetaRepository;
import com.ani.taku_backend.chatroom.domain.repository.ChatRoomRepository;
import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Collections;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 채팅 메시지 관련 쿼리(Query) 작업을 담당하는 서비스
 * 메시지 조회, 접근 권한 검증 등 읽기 전용 기능을 제공합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatMessageQueryService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMetaRepository chatRoomMetaRepository;

    /**
     * 채팅방의 메시지를 조회합니다.
     * messageId가 없으면 최신 메시지를, 있으면 해당 메시지 이전의 메시지를 반환합니다.
     */
    public ChatMessageListResponseDTO getChatMessages(String wsRoomId, String messageId, int limit) {
        ChatRoom chatRoom = chatRoomRepository.findByWsRoomId(wsRoomId)
                .orElseThrow(() -> new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        ChatRoomMetaInfo metaInfo = chatRoomMetaRepository.findByChatRoomId(chatRoom.getId())
                .orElseThrow(() -> new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND));
        
        List<ChatMessage> messages;
        
        if (messageId == null || messageId.isEmpty()) {
            messages = metaInfo.getRecentMessages(limit);
        } else {
            Optional<ChatMessage> targetMessage = metaInfo.findMessageById(messageId);
            
            if (targetMessage.isPresent()) {
                LocalDateTime sentAt = targetMessage.get().getSentAt();
                messages = metaInfo.getMessagesBeforeTime(sentAt, limit);
            } else {
                messages = new ArrayList<>();
            }
        }

        boolean hasMore = messages.size() >= limit;

        return new ChatMessageListResponseDTO(ChatMessage.toResponseDTOList(messages), hasMore);
    }

    /**
     * 채팅방 접근 권한을 검증합니다.
     */
    public void validateChatRoomAccess(String wsRoomId, Long userId) {
        log.debug("채팅방 접근 권한 검증: wsRoomId={}, userId={}", wsRoomId, userId);

        // 1. 채팅방 엔티티 조회
        ChatRoom chatRoom = chatRoomRepository.findByWsRoomId(wsRoomId)
                .orElseThrow(() -> new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        // 2. 채팅방 메타 정보 조회
        ChatRoomMetaInfo metaInfo = chatRoomMetaRepository.findByChatRoomId(chatRoom.getId())
                .orElseThrow(() -> new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        // 3. 도메인 객체에 로직 위임
        metaInfo.validateUserAccess(userId);
    }

    /**
     * 여러 채팅방의 마지막 메시지를 한 번에 가져옵니다.
     */
    public Map<Long, ChatMessage> getLastMessageMap(List<Long> chatRoomIds) {
        if (chatRoomIds == null || chatRoomIds.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, ChatMessage> result = chatRoomMetaRepository.getLastMessageMap(chatRoomIds);
        
        int missingRooms = chatRoomIds.size() - result.size();
        if (missingRooms > 0) {
            log.debug("일부 채팅방({})의 마지막 메시지를 찾을 수 없습니다", missingRooms);
        }
        
        return result;
    }

    /**
     * WebSocket 채팅방 ID를 실제 채팅방 ID로 변환합니다.
     * 
     * @param wsRoomId WebSocket 채팅방 ID
     * @return 실제 채팅방 ID 또는 없는 경우 null
     */
    public Long getChatRoomIdFromWsRoomId(String wsRoomId) {
        try {
            ChatRoom chatRoom = chatRoomRepository.findByWsRoomId(wsRoomId)
                    .orElse(null);
            
            return chatRoom != null ? chatRoom.getId() : null;
        } catch (Exception e) {
            log.error("WebSocket ID 변환 중 오류: {}", e.getMessage());
            return null;
        }
    }
} 