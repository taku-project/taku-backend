package com.ani.taku_backend.chatroom.domain.entity;

import com.ani.taku_backend.chatroom.domain.constant.MarketRole;
import com.ani.taku_backend.chatroom.util.ParticipantUtils;
import com.ani.taku_backend.common.baseEntity.BaseTimeEntity;
import com.ani.taku_backend.user.model.entity.User;
import jakarta.persistence.*;
import lombok.*;

/**
 * 채팅방 참여자 정보를 나타내는 엔티티입니다.
 * 한 명의 사용자는 여러 채팅방에 참여할 수 있으며, 각 채팅방에는 여러 사용자가 참여할 수 있습니다.
 * ChatRoom과 User 사이의 다대다 관계를 관리합니다.
 * 
 * 주요 책임:
 * 1. User 엔티티와의 관계 관리 (JPA 연관관계)
 * 2. 참여자의 기본 정보 및 영구 데이터 저장
 * 3. 트랜잭션이 필요한 작업 처리
 */
@Entity
@Getter
@Table(name = "chat_room_participant", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"chat_room_id", "user_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoomParticipant extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    // 참여자 역할 (BUYER 또는 SELLER)
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private MarketRole role;
    
    @Column(name = "unread_count")
    private Integer unreadCount = 0;

    @Column(name = "is_connected")
    private Boolean isConnected = false;
    
    @Column(name = "last_read_message_id")
    private Long lastReadMessageId;

    /**
     * 채팅방 참여자 생성
     */
    @Builder
    public ChatRoomParticipant(ChatRoom chatRoom, User user, MarketRole role) {
        this.chatRoom = chatRoom;
        this.user = user;
        this.role = role;
        this.unreadCount = 0;
        this.isConnected = false;
    }

    /**
     * 채팅방 엔티티를 설정합니다 (양방향 관계 관리용)
     */
    void setChatRoom(ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
    }

    /**
     * 연결 상태를 변경합니다
     */
    public void setConnected(boolean connected) {
        this.isConnected = connected;
    }

    /**
     * 읽지 않은 메시지 수를 설정합니다
     */
    public void setUnreadCount(int count) {
        this.unreadCount = count;
    }

    /**
     * 읽지 않은 메시지 수를 증가시킵니다
     */
    public void incrementUnreadCount() {
        this.unreadCount++;
    }

    /**
     * 읽지 않은 메시지 카운터를 초기화합니다
     */
    public void resetUnreadCount() {
        this.unreadCount = 0;
    }

    /**
     * 마지막으로 읽은 메시지 ID를 설정합니다
     */
    public void setLastReadMessageId(Long messageId) {
        this.lastReadMessageId = messageId;
    }
    
    /**
     * 참여자가 판매자인지 확인합니다.
     * 
     * @return 판매자인 경우 true
     */
    public boolean isSeller() {
        return ParticipantUtils.isSeller(this.role);
    }

    /**
     * 참여자가 구매자인지 확인합니다.
     * 
     * @return 구매자인 경우 true
     */
    public boolean isBuyer() {
        return ParticipantUtils.isBuyer(this.role);
    }
} 