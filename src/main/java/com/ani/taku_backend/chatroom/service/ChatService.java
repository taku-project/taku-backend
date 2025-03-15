package com.ani.taku_backend.chatroom.service;

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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.Optional;
import java.util.Collections;

/**
 * 채팅 메시지 및 메시지 통신 관련 기능을 담당하는 서비스
 * 메시지 송수신, 읽음 처리, 메시지 조회 등의 기능을 제공합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMetaRepository chatRoomMetaRepository;

    /**
     * WebSocket을 통해 받은 메시지를 처리하고 저장합니다.
     */
    @Transactional
    public ChatMessage saveAndProcessMessage(String wsRoomId, Long senderId, String content) {
        log.debug("메시지 저장 요청: roomId={}, senderId={}", wsRoomId, senderId);

        ChatRoom chatRoom = chatRoomRepository.findByWsRoomId(wsRoomId)
                .orElseThrow(() -> new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        ChatMessage message = ChatMessage.of(chatRoom.getId(), chatRoom.getArticleId(), senderId, content);

        ChatRoomMetaInfo metaInfo = chatRoomMetaRepository.findByChatRoomId(chatRoom.getId())
                .orElseThrow(() -> new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        metaInfo.addMessage(message);

        chatRoomMetaRepository.save(metaInfo);

        
        return message;
    }

    /**
     * 사용자가 채팅방을 나갈 때 처리하는 메서드입니다.
     */
    @Transactional
    public void leaveRoomByWsRoomId(String wsRoomId, Long userId) {

        ChatRoom chatRoom = chatRoomRepository.findByWsRoomId(wsRoomId)
                .orElseThrow(() -> new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        ChatRoomMetaInfo chatRoomMetaInfo = chatRoomMetaRepository.findById(String.valueOf(chatRoom.getId()))
                .orElseThrow(() -> new DuckwhoException(ErrorCode.DUPLICATE_CHAT_ROOM));

        if(!chatRoomMetaInfo.getParticipants().containsUser(userId)){
            throw new DuckwhoException(ErrorCode.INVALID_CHAT_USER);
        }

        chatRoomMetaInfo.getParticipants().setDisconnected(userId);
        chatRoomMetaInfo.checkAndDeactivate();

        chatRoomMetaRepository.save(chatRoomMetaInfo);

        if (chatRoomMetaInfo.getParticipants().isAllDisconnected()) {
            chatRoom.deactivate();
            chatRoomRepository.save(chatRoom);
        }
    }

    /**
     * 채팅방의 읽지 않은 메시지를 모두 읽음 상태로 표시합니다.
     */
    @Transactional
    public void markMessagesAsReadByWsRoomId(String wsRoomId, Long userId) {

        ChatRoom chatRoom = chatRoomRepository.findByWsRoomId(wsRoomId)
                .orElseThrow(() -> new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        ChatRoomMetaInfo metaInfo = chatRoomMetaRepository.findByChatRoomId(chatRoom.getId())
                .orElseThrow(() -> new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        metaInfo.markMessagesAsRead(userId);
        metaInfo.resetUnreadCount(userId);

        chatRoomMetaRepository.save(metaInfo);

    }

    /**
     * 비동기로 메시지 읽음 상태를 업데이트합니다.
     */
    @Async
    @Transactional
    public void markMessagesAsReadAsync(String wsRoomId, Long userId) {
        try {
            markMessagesAsReadByWsRoomId(wsRoomId, userId);
        } catch (Exception e) {
            log.error("Error marking messages as read: {}", e.getMessage(), e);
        }
    }

    /**
     * 채팅방의 메시지를 조회합니다.
     * messageId가 없으면 최신 메시지를, 있으면 해당 메시지 이전의 메시지를 반환합니다.
     */
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
    public void validateChatRoomAccess(String wsRoomId, Long userId) {

        ChatRoom chatRoom = chatRoomRepository.findByWsRoomId(wsRoomId)
                .orElseThrow(() -> new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND));


        ChatRoomMetaInfo metaInfo = chatRoomMetaRepository.findByChatRoomId(chatRoom.getId())
                .orElseThrow(() -> new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND));
        if (!metaInfo.getParticipants().containsUser(userId)) {
            throw new DuckwhoException(ErrorCode.INVALID_CHAT_USER);
        }
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
        if (missingRooms > 0) {;
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
            return null;
        }
    }
}


