package com.ani.taku_backend.chatroom.domain.entity;

import com.ani.taku_backend.chatroom.domain.constant.ChatRoomStatus;
import com.ani.taku_backend.chatroom.domain.constant.JangterChatRole;
import com.ani.taku_backend.common.baseEntity.BaseTimeEntity;
import com.ani.taku_backend.user.model.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.UUID;
import java.util.Optional;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

/**
 * 채팅방 정보를 나타내는 엔티티입니다.
 * 각 채팅방은 고유의 WebSocket ID와 관련 상품 정보를 가집니다.
 */
@Entity
@Table(name = "chat_room")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String wsRoomId;

    @Column
    private Long articleId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ChatRoomStatus status = ChatRoomStatus.ACTIVE;

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<ChatRoomParticipant> participants = new LinkedHashSet<>();

    /**
     * 채팅방에 참여자를 추가합니다.
     * 
     * @param participant 추가할 참여자
     * @return 추가된 참여자
     */
    public ChatRoomParticipant addParticipant(ChatRoomParticipant participant) {
        this.participants.add(participant);
        return participant;
    }

    /**
     * 채팅방 ID를 생성합니다.
     * 신규 채팅방 생성 시 UUID 기반의 고유 ID를 생성합니다.
     */
    @PrePersist
    public void generateWsRoomId() {
        if (this.wsRoomId == null) {
            this.wsRoomId = UUID.randomUUID().toString();
        }
    }

    /**
     * 채팅방을 비활성화합니다.
     */
    public void deactivate() {
        this.status = ChatRoomStatus.INACTIVE;
    }

    /**
     * 해당 역할을 가진 참여자를 찾습니다.
     * 
     * @param role 찾을 역할
     * @return 해당 역할을 가진 참여자 목록
     */
    public List<ChatRoomParticipant> findParticipantsByRole(JangterChatRole role) {
        return this.participants.stream()
                .filter(p -> p.getRole() == role)
                .collect(Collectors.toList());
    }

    /**
     * 채팅방의 구매자를 찾습니다.
     * 
     * @return 구매자의 User 객체, 없으면 null
     */
    public User getBuyer() {
        return findParticipantsByRole(JangterChatRole.BUYER).stream()
                .findFirst()
                .map(ChatRoomParticipant::getUser)
                .orElse(null);
    }

    /**
     * 채팅방의 판매자를 찾습니다.
     * 
     * @return 판매자의 User 객체, 없으면 null
     */
    public User getSeller() {
        return findParticipantsByRole(JangterChatRole.SELLER).stream()
                .findFirst()
                .map(ChatRoomParticipant::getUser)
                .orElse(null);
    }

    /**
     * 사용자 ID와 역할로 새로운 채팅방을 생성합니다.
     * 
     * @param articleId 상품 ID
     * @param buyer 구매자
     * @param seller 판매자
     * @return 생성된 채팅방 인스턴스
     */
    public static ChatRoom createChatRoom(Long articleId, User buyer, User seller) {
        ChatRoom chatRoom = ChatRoom.builder()
                .articleId(articleId)
                .build();

        ChatRoomParticipant buyerParticipant = ChatRoomParticipant.builder()
                .chatRoom(chatRoom)
                .user(buyer)
                .role(JangterChatRole.BUYER)
                .build();

        ChatRoomParticipant sellerParticipant = ChatRoomParticipant.builder()
                .chatRoom(chatRoom)
                .user(seller)
                .role(JangterChatRole.SELLER)
                .build();

        chatRoom.addParticipant(buyerParticipant);
        chatRoom.addParticipant(sellerParticipant);

        return chatRoom;
    }
}