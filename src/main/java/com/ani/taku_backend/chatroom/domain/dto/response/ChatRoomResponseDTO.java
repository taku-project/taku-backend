package com.ani.taku_backend.chatroom.domain.dto.response;

import com.ani.taku_backend.chatroom.domain.document.ChatMessage;
import com.ani.taku_backend.chatroom.domain.entity.ChatRoom;
import com.ani.taku_backend.chatroom.util.ChatDateTimeFormatter;

import java.time.LocalDateTime;

public record ChatRoomResponseDTO(
        Long id,
        String roomId,
        Long articleId,
        Long buyerId,
        Long sellerId,
        LocalDateTime createdAt,
        String buyerNickname,
        String sellerNickname,
        String lastMessage,
        String lastMessageTime,
        Long lastMessageSenderId,
        Integer unreadCount,
        String articleThumbnailUrl,
        String buyerProfileImageUrl,
        String sellerProfileImageUrl
) {

    /**
     * 채팅방과 관련 정보로부터 응답 DTO를 생성합니다.
     */
    public static ChatRoomResponseDTO from(
            ChatRoom chatRoom,
            Long buyerId,
            Long sellerId,
            String buyerNickname,
            String sellerNickname,
            ChatMessage lastMessage,
            Integer unreadCount,
            String articleThumbnailUrl,
            String buyerProfileImageUrl,
            String sellerProfileImageUrl
    ) {
        LocalDateTime messageTime = lastMessage != null ? lastMessage.getSentAt() : null;

        return new ChatRoomResponseDTO(
                chatRoom.getId(),
                chatRoom.getWsRoomId(),
                chatRoom.getArticleId(),
                buyerId,
                sellerId,
                chatRoom.getCreatedAt(),
                buyerNickname,
                sellerNickname,
                lastMessage != null ? lastMessage.getContent() : null,
                ChatDateTimeFormatter.formatMessageTime(messageTime),
                lastMessage != null ? lastMessage.getSenderId() : null,
                unreadCount,
                articleThumbnailUrl,
                buyerProfileImageUrl,
                sellerProfileImageUrl
        );
    }
}