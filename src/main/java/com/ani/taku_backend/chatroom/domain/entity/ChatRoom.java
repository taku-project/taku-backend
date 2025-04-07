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
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;
import java.util.Objects;
import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.exception.ErrorCode;

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

    /**
     * 채팅방의 유효성을 검증합니다.
     * 필수 참여자(구매자 또는 판매자)가 없는 경우 false를 반환합니다.
     *
     * @return 채팅방 유효성 여부
     */
    public boolean isValid() {
        return getBuyer() != null || getSeller() != null;
    }

    /**
     * 채팅방의 상태를 검증합니다.
     * 비활성화된 채팅방이거나 유효하지 않은 경우 예외를 발생시킵니다.
     * 
     * @throws DuckwhoException 채팅방이 비활성 상태이거나 유효하지 않은 경우
     */
    public void validateStatus() {
        if (!isValid()) {
            throw new DuckwhoException(ErrorCode.INACTIVE_CHAT_ROOM);
        }
        
        if (this.status != ChatRoomStatus.ACTIVE) {
            throw new DuckwhoException(ErrorCode.INACTIVE_CHAT_ROOM);
        }
    }
    
    /**
     * 사용자가 채팅방에 참여 가능한지 검증합니다.
     * 해당 사용자가 채팅방 참여자가 아닌 경우 예외를 발생시킵니다.
     * 
     * @param userId 검증할 사용자 ID
     * @throws DuckwhoException 사용자가 참여자가 아닌 경우
     */
    public void validateUserAccess(Long userId) {
        boolean isParticipant = this.participants.stream()
                .anyMatch(p -> p.isUser(userId));
                
        if (!isParticipant) {
            throw new DuckwhoException(ErrorCode.UNAUTHORIZED_ACCESS);
        }
    }

    /**
     * 여러 채팅방에서 채팅방 ID 목록을 추출합니다.
     * 
     * @param chatRooms 채팅방 목록
     * @return 채팅방 ID 목록
     */
    public static List<Long> extractChatRoomIds(List<ChatRoom> chatRooms) {
        return chatRooms.stream()
                .map(ChatRoom::getId)
                .collect(Collectors.toList());
    }
    
    /**
     * 여러 채팅방에서 중복 없는 상품 ID 목록을 추출합니다.
     * 
     * @param chatRooms 채팅방 목록
     * @return 중복 없는 상품 ID 목록
     */
    public static List<Long> extractArticleIds(List<ChatRoom> chatRooms) {
        return chatRooms.stream()
                .map(ChatRoom::getArticleId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 특정 사용자가 특정 역할로 참여하고 있는지 확인합니다.
     * 
     * @param userId 확인할 사용자 ID
     * @param role 확인할 역할
     * @return 해당 사용자가 지정된 역할로 참여하고 있으면 true
     */
    public boolean hasUserWithRole(Long userId, JangterChatRole role) {
        return this.participants.stream()
                .anyMatch(participant -> 
                        participant.isUser(userId) && 
                        participant.getRole() == role);
    }
    
    /**
     * 활성 상태의 채팅방 목록에서 특정 사용자가 구매자로 참여하고 있는 채팅방이 있는지 확인합니다.
     * 
     * @param chatRooms 확인할 채팅방 목록
     * @param buyerId 확인할 구매자 ID
     * @return 구매자로 참여중인 활성 채팅방이 있으면 true
     */
    public static boolean hasActiveBuyerInRooms(List<ChatRoom> chatRooms, Long buyerId) {
        return chatRooms.stream()
                .filter(room -> room.getStatus() == ChatRoomStatus.ACTIVE)
                .anyMatch(room -> room.hasUserWithRole(buyerId, JangterChatRole.BUYER));
    }

}