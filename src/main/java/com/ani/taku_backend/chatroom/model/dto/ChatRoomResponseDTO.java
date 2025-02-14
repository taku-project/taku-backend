package com.ani.taku_backend.chatroom.model.dto;

import com.ani.taku_backend.chatroom.model.entity.ChatRoom;
import java.time.LocalDateTime;

public record ChatRoomResponseDTO(
        Long id,
        String roomId,
        Long articleId,
        Long buyerId,
        Long sellerId,
        LocalDateTime createdAt
) {
    public static ChatRoomResponseDTO of(ChatRoom chatRoom, Long buyerId, Long sellerId) {
        return new ChatRoomResponseDTO(
                chatRoom.getId(),
                chatRoom.getWsRoomId(),
                chatRoom.getArticleId(),
                buyerId,
                sellerId,
                chatRoom.getCreatedAt()
        );
    }
}
