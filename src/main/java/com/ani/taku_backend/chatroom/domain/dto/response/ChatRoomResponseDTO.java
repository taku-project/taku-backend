package com.ani.taku_backend.chatroom.domain.dto.response;

import com.ani.taku_backend.chatroom.domain.entity.ChatRoom;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ChatRoomResponseDTO(
        Long chatRoomId,
        String wsRoomId,
        Long articleId,
        Long buyerId,
        Long sellerId,
        String buyerNickname,
        String sellerNickname,
        ChatMessageResponseDTO lastMessage,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Integer unreadMessageCount,
        String articleImageUrl,
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
            ChatMessageResponseDTO lastMessageDTO,
            Integer unreadCount,
            String articleImageUrl,
            String buyerProfileImageUrl,
            String sellerProfileImageUrl
    ) {
        return ChatRoomResponseDTO.builder()
                .chatRoomId(chatRoom.getId())
                .wsRoomId(chatRoom.getWsRoomId())
                .articleId(chatRoom.getArticleId())
                .buyerId(buyerId)
                .sellerId(sellerId)
                .buyerNickname(buyerNickname)
                .sellerNickname(sellerNickname)
                .lastMessage(lastMessageDTO)
                .createdAt(chatRoom.getCreatedAt())
                .updatedAt(chatRoom.getUpdatedAt())
                .unreadMessageCount(unreadCount)
                .articleImageUrl(articleImageUrl)
                .buyerProfileImageUrl(buyerProfileImageUrl)
                .sellerProfileImageUrl(sellerProfileImageUrl)
                .build();
    }
}