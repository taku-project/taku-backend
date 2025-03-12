package com.ani.taku_backend.chatroom.domain.dto;

import com.ani.taku_backend.chatroom.util.ChatDateTimeFormatter;
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

    /**
     * 포맷팅된 마지막 메시지 시간을 반환합니다.
     */
    public String getLastMessageTime() {
        return ChatDateTimeFormatter.formatMessageTime(lastMessageSentAt);
    }

    /**
     * 상품 썸네일 URL을 설정합니다.
     */
    public void setArticleThumbnailUrl(String url) {
        this.articleThumbnailUrl = url;
    }
}