package com.ani.taku_backend.chatroom.service;

import com.ani.taku_backend.chatroom.domain.document.ChatMessage;
import com.ani.taku_backend.chatroom.domain.dto.response.ChatMessageListResponseDTO;
import com.ani.taku_backend.chatroom.service.command.ChatMessageCommandService;
import com.ani.taku_backend.chatroom.service.query.ChatMessageQueryService;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 채팅 메시지 관련 기능을 통합해서 제공하는 파사드 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageCommandService commandService;
    private final ChatMessageQueryService queryService;

    /**
     * WebSocket을 통해 받은 메시지를 처리하고 저장합니다.
     */
    public ChatMessage saveAndProcessMessage(String wsRoomId, Long senderId, String content) {
        return commandService.saveAndProcessMessage(wsRoomId, senderId, content);
    }

    /**
     * 사용자가 채팅방을 나갈 때 처리하는 메서드입니다.
     */
    public void leaveRoom(String wsRoomId, Long userId) {
        commandService.leaveRoom(wsRoomId, userId);
    }

    /**
     * 채팅방의 읽지 않은 메시지를 모두 읽음 상태로 표시합니다.
     */
    public void markMessagesAsRead(String wsRoomId, Long userId) {
        commandService.markMessagesAsRead(wsRoomId, userId);
    }

    /**
     * 비동기로 메시지 읽음 상태를 업데이트합니다.
     */
    public void markMessagesAsReadAsync(String wsRoomId, Long userId) {
        commandService.markMessagesAsReadAsync(wsRoomId, userId);
    }

    /**
     * 채팅방의 메시지를 조회합니다.
     */
    public ChatMessageListResponseDTO getChatMessages(String wsRoomId, String messageId, int limit) {
        return queryService.getChatMessages(wsRoomId, messageId, limit);
    }

    /**
     * 채팅방 접근 권한을 검증합니다.
     */
    public void validateChatRoomAccess(String wsRoomId, Long userId) {
        queryService.validateChatRoomAccess(wsRoomId, userId);
    }


    /**
     * WebSocket 채팅방 ID를 실제 채팅방 ID로 변환합니다.
     */
    public Long getChatRoomIdFromWsRoomId(String wsRoomId) {
        return queryService.getChatRoomIdFromWsRoomId(wsRoomId);
    }
} 