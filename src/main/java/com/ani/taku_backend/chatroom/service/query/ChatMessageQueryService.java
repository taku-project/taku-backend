package com.ani.taku_backend.chatroom.service.query;

import com.ani.taku_backend.chatroom.domain.document.ChatRoomMetaInfo;
import com.ani.taku_backend.chatroom.domain.document.ChatMessage;
import com.ani.taku_backend.chatroom.dto.response.ChatMessageListResponseDTO;
import com.ani.taku_backend.chatroom.domain.entity.ChatRoom;
import com.ani.taku_backend.chatroom.domain.repository.ChatRoomMetaRepository;
import com.ani.taku_backend.chatroom.domain.repository.ChatRoomRepository;
import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    private final ChatRoomQueryService chatRoomQueryService;

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
                messages = getMessagesBeforeTime(chatRoom.getId(), sentAt, limit);
            } else {
                messages = new ArrayList<>();
            }
        }

        boolean hasMore = messages.size() >= limit;

        return new ChatMessageListResponseDTO(ChatMessage.toResponseDTOList(messages), hasMore);
    }

    /**
     * 특정 시간 이전의 메시지를 조회합니다. (무한 스크롤용)
     *
     * @param chatRoomId 채팅방 ID
     * @param before 기준 시간
     * @param limit 조회할 메시지 수
     * @return 조건에 맞는 메시지 목록
     */
    public List<ChatMessage> getMessagesBeforeTime(Long chatRoomId, LocalDateTime before, int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "sentAt"));
        return chatRoomMetaRepository.findMessagesByChatRoomIdAndSentAtBeforeOrderBySentAtDesc(
            chatRoomId, before, pageable);
    }

    /**
     * 채팅방 접근 권한을 검증합니다.
     * 
     * @param wsRoomId 웹소켓 채팅방 ID
     * @param userId 사용자 ID
     * @throws DuckwhoException 채팅방이 없거나 접근 권한이 없는 경우
     */
    public void validateChatRoomAccess(String wsRoomId, Long userId) {
        chatRoomQueryService.validateChatRoomAccess(wsRoomId, userId);
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