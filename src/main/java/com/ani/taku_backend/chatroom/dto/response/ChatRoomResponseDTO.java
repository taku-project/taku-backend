package com.ani.taku_backend.chatroom.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 채팅방 정보를 응답하기 위한 DTO 클래스
 */
@Getter
@ToString
public class ChatRoomResponseDTO {

    private final Long chatRoomId;
    private final String wsRoomId;
    private final Long articleId;
    private final Long buyerId;
    private final Long sellerId;
    private final String buyerNickname;
    private final String sellerNickname;
    private final ChatMessageResponseDTO lastMessage;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final String articleImageUrl;
    private final Integer unreadMessageCount;
    private final String buyerProfileImageUrl;
    private final String sellerProfileImageUrl;

    @Builder
    public ChatRoomResponseDTO(
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
            String articleImageUrl,
            Integer unreadMessageCount,
            String buyerProfileImageUrl,
            String sellerProfileImageUrl) {
        this.chatRoomId = chatRoomId;
        this.wsRoomId = wsRoomId;
        this.articleId = articleId;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.buyerNickname = buyerNickname;
        this.sellerNickname = sellerNickname;
        this.lastMessage = lastMessage;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.articleImageUrl = articleImageUrl;
        this.unreadMessageCount = unreadMessageCount;
        this.buyerProfileImageUrl = buyerProfileImageUrl;
        this.sellerProfileImageUrl = sellerProfileImageUrl;
    }
}