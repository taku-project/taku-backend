package com.ani.taku_backend.chatroom.dto;

import com.ani.taku_backend.chatroom.contansts.MessageConstants;
import com.ani.taku_backend.chatroom.domain.document.ChatRoomMetaInfo;
import com.ani.taku_backend.chatroom.domain.entity.ChatRoom;
import com.ani.taku_backend.user.model.entity.User;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * 채팅방 생성 결과와 응답 DTO 변환에 필요한 모든 정보
 */
@Getter
public class ChatRoomAggregateResult {
    private final ChatRoom chatRoom;
    private final ChatRoomMetaInfo metaInfo;
    private final User buyer;
    private final User seller;
    private final String articleImage;
    private final String articleTitle;
    private final BigDecimal articlePrice;
    
    public ChatRoomAggregateResult(
            ChatRoom chatRoom,
            ChatRoomMetaInfo metaInfo,
            User buyer,
            User seller,
            String articleImage,
            String articleTitle,
            BigDecimal articlePrice) {
        this.chatRoom = Objects.requireNonNull(chatRoom, MessageConstants.CHAT_ROOM_NOT_NULL);
        this.metaInfo = Objects.requireNonNull(metaInfo, MessageConstants.META_INFO_NOT_NULL);
        this.buyer = Objects.requireNonNull(buyer, MessageConstants.BUYER_NOT_NULL);
        this.seller = Objects.requireNonNull(seller, MessageConstants.SELLER_NOT_NULL);
        this.articleImage = articleImage;
        this.articleTitle = articleTitle;
        this.articlePrice = articlePrice;
    }
} 