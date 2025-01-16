package com.ani.taku_backend.chatroom.model.entity;

import com.ani.taku_backend.chatroom.model.constant.ChatRoomStatus;
import com.ani.taku_backend.common.baseEntity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "chat_room")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_id", unique = true)
    private String roomId;  // WebSocket 세션 관리용 ID

    @Column(name = "article_id", nullable = false)
    private Long articleId;  //판매글 id

    @Column(name = "buyer_id", nullable = false)
    private Long buyerId;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "last_message_id")
    private Long lastMessageId = 0L;

    @Column(name = "buyer_last_read_message_id")
    private Long buyerLastReadMessageId = 0L;

    @Column(name = "seller_last_read_message_id")
    private Long sellerLastReadMessageId = 0L;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ChatRoomStatus status = ChatRoomStatus.ACTIVE;

    @Builder
    public ChatRoom(Long articleId, Long buyerId, Long sellerId) {
        this.roomId = UUID.randomUUID().toString();
        this.articleId = articleId;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
    }

    public void updateLastMessage(Long messageId) {
        this.lastMessageId = messageId;
    }

    public void updateBuyerLastRead(Long messageId) {
        this.buyerLastReadMessageId = messageId;
    }

    public void updateSellerLastRead(Long messageId) {
        this.sellerLastReadMessageId = messageId;
    }

    public void deactivate() {
        this.status = ChatRoomStatus.INACTIVE;
    }
}