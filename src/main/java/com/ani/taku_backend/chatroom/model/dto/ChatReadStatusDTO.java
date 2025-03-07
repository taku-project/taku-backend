package com.ani.taku_backend.chatroom.model.dto;

import java.time.LocalDateTime;

/**
 * 채팅 메시지 읽음 상태 정보를 전달하기 위한 경량화된 DTO 입니다.
 */
public record ChatReadStatusDTO(
    Long chatRoomId,
    Long senderId,
    LocalDateTime timestamp
) {

    public static ChatReadStatusDTO of(Long chatRoomId, Long senderId) {
        return new ChatReadStatusDTO(chatRoomId, senderId, LocalDateTime.now());
    }
} 