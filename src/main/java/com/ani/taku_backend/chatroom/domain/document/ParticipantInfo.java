package com.ani.taku_backend.chatroom.domain.document;

import com.ani.taku_backend.chatroom.domain.constant.JangterChatRole;
import lombok.Getter;
import org.springframework.data.repository.query.Param;
import java.time.Instant;

/**
 * 채팅방 참가자 정보를 나타냅니다.
 * 사용자 ID, 역할, 활성화 상태, 메세지 스톡, 마지막 비활성화 시간을 관리합니다.
 */
@Getter
public class ParticipantInfo {

    //메세지 스톡의 초기화 값
    private static final Integer INITIAL_MESSAGE_STOCK = 0;
    
    private Long userId;

    private JangterChatRole role;

    private Boolean isActive;
    private Integer messageStock;
    private Instant lastDeactivatedAt;


    public ParticipantInfo() {
    }

    public ParticipantInfo(@Param("userId") Long userId, @Param("role") JangterChatRole role) {
        this.userId = userId;
        this.role = role;
        this.isActive = true;
        this.lastDeactivatedAt = null;
        this.messageStock = INITIAL_MESSAGE_STOCK;
    }

    /**
     * 참여자의 활성화 상태를 반환합니다.
     * 
     * @return 활성화 상태
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(this.isActive);
    }

    public void plusMessage() {
        this.messageStock++;
    }

    /**
     * 참여자의 메시지 카운터를 초기화합니다.
     */
    public void resetMessageStock() {
        this.messageStock = 0;
    }

    /**
     * 참여자를 활성화 상태로 설정합니다.
     */
    public void activate() {
        this.isActive = true;
    }

    /**
     * 참여자를 비활성화 상태로 설정하고 마지막 비활성화 시간을 현재로 업데이트합니다.
     */
    public void deactivate() {
        this.isActive = false;
        this.lastDeactivatedAt = Instant.now();
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
}