package com.ani.taku_backend.chatroom.model.entity;

import com.ani.taku_backend.chatroom.model.constant.ChatRoomStatus;
import com.ani.taku_backend.chatroom.model.constant.ChatRoomLeftBy;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ChatRoomStatus status = ChatRoomStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "left_by")
    private ChatRoomLeftBy leftBy = ChatRoomLeftBy.NONE;

    @Builder
    public ChatRoom(Long articleId, Long buyerId, Long sellerId) {
        this.roomId = UUID.randomUUID().toString();
        this.articleId = articleId;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
    }

    /**
     * 채팅방을 비활성화하고 나간 사용자를 기록합니다.
     * @param userId 채팅방을 나가는 사용자의 ID
     * @throws IllegalArgumentException 채팅방 참여자가 아닌 경우
     */
    public void deactivate(Long userId) {
        if (!isParticipant(userId)) {
            throw new IllegalArgumentException("채팅방 참여자가 아닙니다.");
        }
        this.status = ChatRoomStatus.INACTIVE;
        this.leftBy = userId.equals(buyerId) ? ChatRoomLeftBy.BUYER : ChatRoomLeftBy.SELLER;
    }

    /**
     * 주어진 사용자가 채팅방의 참여자(구매자 또는 판매자)인지 확인합니다.
     * @param userId 확인할 사용자의 ID
     * @return 채팅방 참여자이면 true, 아니면 false
     */
    public boolean isParticipant(Long userId) {
        return userId.equals(buyerId) || userId.equals(sellerId);
    }

    /**
     * 주어진 사용자가 메시지를 보낼 수 있는지 확인합니다.
     * 채팅방이 활성화 상태이거나, 사용자가 나가지 않은 상태여야 메시지를 보낼 수 있습니다.
     * @param userId 확인할 사용자의 ID
     * @return 메시지를 보낼 수 있으면 true, 없으면 false
     */
    public boolean canSendMessage(Long userId) {
        if (!isParticipant(userId)) {
            return false;
        }
        // 채팅방이 비활성화 상태이고, 나간 사람이면 메시지를 보낼 수 없음
        boolean isBuyer = userId.equals(buyerId);
        return status.isActive() || 
               (isBuyer && leftBy != ChatRoomLeftBy.BUYER) || 
               (!isBuyer && leftBy != ChatRoomLeftBy.SELLER);
    }

    /**
     * 채팅방을 나간 사용자에 대한 메시지를 반환합니다.
     * @return 채팅방 퇴장 메시지 (예: "구매자가 채팅방을 나갔습니다.")
     */
    public String getExitMessage() {
        return leftBy.getMessage();
    }

    /**
     * 주어진 사용자가 채팅방을 나갔는지 확인합니다.
     * @param userId 확인할 사용자의 ID
     * @return 사용자가 채팅방을 나갔으면 true, 아니면 false
     */
    public boolean hasUserLeft(Long userId) {
        boolean isBuyer = userId.equals(buyerId);
        return (isBuyer && leftBy == ChatRoomLeftBy.BUYER) || 
               (!isBuyer && leftBy == ChatRoomLeftBy.SELLER);
    }
}