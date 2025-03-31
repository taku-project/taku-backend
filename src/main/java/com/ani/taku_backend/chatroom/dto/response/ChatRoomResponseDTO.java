package com.ani.taku_backend.chatroom.dto.response;

import com.ani.taku_backend.chatroom.contansts.MessageConstants;
import com.ani.taku_backend.chatroom.domain.document.ChatMessage;
import com.ani.taku_backend.chatroom.domain.document.ChatRoomMetaInfo;
import com.ani.taku_backend.chatroom.domain.entity.ChatRoom;
import com.ani.taku_backend.chatroom.domain.vo.ArticleImage;
import com.ani.taku_backend.chatroom.domain.vo.ChatRoomMessages;
import com.ani.taku_backend.chatroom.domain.vo.ChatRoomUsers;
import com.ani.taku_backend.chatroom.domain.vo.UnreadMessageCounts;

import com.ani.taku_backend.user.model.entity.User;
import com.fasterxml.jackson.annotation.JsonFormat;
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
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime createdAt;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
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