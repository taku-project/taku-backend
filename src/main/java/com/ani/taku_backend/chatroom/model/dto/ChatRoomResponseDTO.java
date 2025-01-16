package com.ani.taku_backend.chatroom.model.dto;

import com.ani.taku_backend.chatroom.model.entity.ChatRoom;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatRoomResponseDTO {
    private Long id;
    private String roomId;
    private Long articleId;
    private Long buyerId;
    private Long sellerId;
    private Long lastMessageId;
    private Long unreadCount;
    private LocalDateTime createdAt;

    public static ChatRoomResponseDTO of(ChatRoom chatRoom, Long userId) {
        long unreadCount = userId.equals(chatRoom.getBuyerId())
                ? chatRoom.getLastMessageId() - chatRoom.getBuyerLastReadMessageId()
                : chatRoom.getLastMessageId() - chatRoom.getSellerLastReadMessageId();

        return new ChatRoomResponseDTO(
                chatRoom.getId(),
                chatRoom.getRoomId(),
                chatRoom.getArticleId(),
                chatRoom.getBuyerId(),
                chatRoom.getSellerId(),
                chatRoom.getLastMessageId(),
                Math.max(0, unreadCount),
                chatRoom.getCreatedAt()
        );
    }
}