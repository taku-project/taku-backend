package com.ani.taku_backend.chatroom.domain.entity;

import com.ani.taku_backend.chatroom.domain.constant.ChatRoomStatus;
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

/**
 * 채팅방 정보를 나타내는 엔티티입니다.
 * 각 채팅방은 고유의 WebSocket ID와 관련 상품 정보를 가집니다.
 */

//TODO USER와 연관관계 매핑

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
    private Long articleId;  //판매글 id


    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ChatRoomStatus status = ChatRoomStatus.ACTIVE;

    @Builder
    public ChatRoom(Long articleId) {
        this.wsRoomId = UUID.randomUUID().toString();
        this.articleId = articleId;
        this.status = ChatRoomStatus.ACTIVE;

    }

    public void deactivate() {
        this.status = ChatRoomStatus.INACTIVE;
    }

    // 테스트 전용 메서드
    @Builder(builderMethodName = "testBuilder")
    public ChatRoom(Long articleId, String wsRoomId) {
        this.wsRoomId = wsRoomId != null ? wsRoomId : UUID.randomUUID().toString();
        this.articleId = articleId;
        this.status = ChatRoomStatus.ACTIVE;
    }
}