package com.ani.taku_backend.chatroom.domain.document;

import com.ani.taku_backend.chatroom.domain.constant.ParticipantRole;
import lombok.Getter;
import org.springframework.data.repository.query.Param;
import java.time.Instant;

/**
 * 채팅방 참가자 정보를 나타냅니다.
 * 사용자 ID, 역할, 연결 상태, 메세지 스톡, 마지막 연결 해제 시간을 관리합니다.
 */
@Getter
public class ParticipantInfo {

    //메세지 스톡의 초기화 값
    private static final Integer INITIAL_MESSAGE_STOCK = 0;
    
    private Long userId;

    private ParticipantRole role;

    private Boolean isConnected;
    private Integer messageStock;
    private Instant lastDisconnectedAt;


    public ParticipantInfo() {
    }

    public ParticipantInfo(@Param("userId") Long userId, @Param("role") ParticipantRole role) {
        this.userId = userId;
        this.role = role;
        this.isConnected = true;
        this.lastDisconnectedAt = Instant.now();
        this.messageStock = INITIAL_MESSAGE_STOCK;
    }

    public void plusMessage() {
        this.messageStock++;
    }

    public void resetMessageStock() {
        this.messageStock = INITIAL_MESSAGE_STOCK;
    }

    public void disconnected() {
        this.isConnected = false;
        this.lastDisconnectedAt = Instant.now();
    }

}