package com.ani.taku_backend.chatroom.domain.entity;

import com.ani.taku_backend.chatroom.domain.constant.ChatRoomStatus;
import com.ani.taku_backend.chatroom.domain.constant.JangterChatRole;
import com.ani.taku_backend.common.baseEntity.BaseTimeEntity;
import com.ani.taku_backend.user.model.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 채팅방 정보를 나타내는 엔티티입니다.
 * 각 채팅방은 고유의 WebSocket ID와 관련 상품 정보를 가집니다.
 * 채팅방은 여러 참여자를 가질 수 있습니다.
 */
@Entity
@Getter
@Table(name = "chat_room")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ws_room_id", unique = true)
    private String wsRoomId;  // WebSocket 세션 관리용 ID

    @Column(name = "article_id", nullable = false)
    private Long articleId;  // 판매글 id

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ChatRoomStatus status = ChatRoomStatus.ACTIVE;

    // 채팅방 참여자 (양방향 매핑)
    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatRoomParticipant> participants = new ArrayList<>();

    @Builder
    public ChatRoom(Long articleId) {
        this.wsRoomId = UUID.randomUUID().toString();
        this.articleId = articleId;
        this.status = ChatRoomStatus.ACTIVE;
    }

    /**
     * 채팅방 상태를 비활성화합니다
     */
    public void deactivate() {
        this.status = ChatRoomStatus.INACTIVE;
    }

    /**
     * 채팅방에 참여자를 추가합니다
     */
    public void addParticipant(ChatRoomParticipant participant) {
        participants.add(participant);
        participant.setChatRoom(this);
    }

    /**
     * 채팅방에서 참여자를 제거합니다
     */
    public void removeParticipant(ChatRoomParticipant participant) {
        participants.remove(participant);
        participant.setChatRoom(null);
    }

    /**
     * 채팅방의 구매자를 찾습니다.
     * @return 구매자 User 또는 null
     */
    public User getBuyer() {
        if (participants == null || participants.isEmpty()) {
            return null;
        }
        return participants.stream()
                .filter(p -> p.getRole() == JangterChatRole.BUYER)
                .findFirst()
                .map(ChatRoomParticipant::getUser)
                .orElse(null);
    }

    /**
     * 채팅방의 판매자를 찾습니다.
     * @return 판매자 User 또는 null
     */
    public User getSeller() {
        if (participants == null || participants.isEmpty()) {
            return null;
        }
        return participants.stream()
                .filter(p -> p.getRole() == JangterChatRole.SELLER)
                .findFirst()
                .map(ChatRoomParticipant::getUser)
                .orElse(null);
    }

}