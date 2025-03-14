package com.ani.taku_backend.chatroom.domain.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 채팅방 상세 정보를 담는 DTO 클래스입니다.
 * QueryDSL을 통해 최적화된 쿼리로 조회되는 결과를 담습니다.
 */
@Getter
@ToString
@Builder
public class ChatRoomDetailDTO {
    private final Long id;
    private final String roomId;
    private final Long articleId;
    private final LocalDateTime createdAt;
    private final Long buyerId;
    private final String buyerNickname;
    private final String buyerProfileImageUrl;
    private final Long sellerId;
    private final String sellerNickname;
    private final String sellerProfileImageUrl;
    private final String lastMessage;
    private final LocalDateTime lastMessageSentAt;
    private final Long lastMessageSenderId;
    private final Integer unreadCount;
    @Builder.Default
    private String articleThumbnailUrl = null;

}