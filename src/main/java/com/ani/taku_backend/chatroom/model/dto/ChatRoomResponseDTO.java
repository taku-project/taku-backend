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
    public static ChatRoomResponseDTO of(ChatRoom chatRoom) {
        return new ChatRoomResponseDTO(
                chatRoom.getId(),
                chatRoom.getRoomId(),
                chatRoom.getArticleId(),
                chatRoom.getBuyerId(),
                chatRoom.getSellerId(),
                chatRoom.getCreatedAt()
        );
    }
}
