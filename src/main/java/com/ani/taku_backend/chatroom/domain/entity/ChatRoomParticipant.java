package com.ani.taku_backend.chatroom.domain.entity;

import com.ani.taku_backend.chatroom.domain.constant.JangterChatRole;
import com.ani.taku_backend.common.baseEntity.BaseTimeEntity;
import com.ani.taku_backend.user.model.entity.User;
import jakarta.persistence.*;
import lombok.*;

/**
 * 채팅방 참여자 정보를 나타내는 엔티티입니다.
 * ChatRoom과 User 사이의 다대다 관계를 관리합니다.
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

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private JangterChatRole role;

    /**
     * 채팅방 참여자 생성
     */
    @Builder
    public ChatRoomParticipant(ChatRoom chatRoom, User user, JangterChatRole role) {
        this.chatRoom = chatRoom;
        this.user = user;
        this.role = role;
    }


    /**
     * 참여자가 판매자인지 확인합니다.
     *
     * @return 판매자인 경우 true
     */
    public boolean isSeller() {
        return role.isSeller();
    }

    /**
     * 참여자가 구매자인지 확인합니다.
     *
     * @return 구매자인 경우 true
     */
    public boolean isBuyer() {
        return role.isBuyer();
    }

    /**
     * 특정 사용자의 참여자인지 확인합니다.
     *
     * @param userId 확인할 사용자 ID
     * @return 일치하면 true, 아니면 false
     */
    public boolean isUser(Long userId) {
        return this.user != null && this.user.getUserId().equals(userId);
    }

    /**
     * 채팅방 정보와 사용자 정보로 참여자 객체를 생성합니다.
     *
     * @param chatRoom 채팅방
     * @param user 사용자
     * @param role 역할
     * @return 생성된 참여자 객체
     */
    public static ChatRoomParticipant createParticipant(ChatRoom chatRoom, User user, JangterChatRole role) {
        ChatRoomParticipant participant = ChatRoomParticipant.builder()
                .chatRoom(chatRoom)
                .user(user)
                .role(role)
                .build();

        if (chatRoom != null) {
            chatRoom.addParticipant(participant);
        }

        return participant;
    }
}