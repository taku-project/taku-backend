package com.ani.taku_backend.chatroom.dto;

import java.time.LocalDateTime;

public record ChatReadStatusDTO(
    Long chatRoomId,
    Long senderId,
    LocalDateTime timestamp
) {

    public static ChatReadStatusDTO of(Long chatRoomId, Long senderId) {
        return new ChatReadStatusDTO(chatRoomId, senderId, LocalDateTime.now());
    }
} 