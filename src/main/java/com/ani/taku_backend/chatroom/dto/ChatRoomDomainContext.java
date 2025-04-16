package com.ani.taku_backend.chatroom.dto;

import com.ani.taku_backend.chatroom.contansts.MessageConstants;
import com.ani.taku_backend.jangter.model.entity.DuckuJangter;
import com.ani.taku_backend.user.model.entity.User;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * 채팅방 생성에 필요한 모든 도메인 객체와 관련 정보를 담는 컨텍스트
 */
@Getter
public class ChatRoomDomainContext {
    private final DuckuJangter product;
    private final User buyer;
    private final User seller;
    private final String articleImage;
    private final String articleTitle;
    private final BigDecimal articlePrice;
    private final boolean hasActiveChatRoom;
    
    public ChatRoomDomainContext(
            DuckuJangter product,
            User buyer,
            User seller,
            String articleImage,
            String articleTitle,
            BigDecimal articlePrice,
            boolean hasActiveChatRoom) {
        this.product = Objects.requireNonNull(product, MessageConstants.PRODUCT_NOT_NULL);
        this.buyer = Objects.requireNonNull(buyer, MessageConstants.BUYER_NOT_NULL);
        this.seller = Objects.requireNonNull(seller, MessageConstants.SELLER_NOT_NULL);
        this.articleImage = articleImage;
        this.articleTitle = articleTitle;
        this.articlePrice = articlePrice;
        this.hasActiveChatRoom = hasActiveChatRoom;
    }
} 