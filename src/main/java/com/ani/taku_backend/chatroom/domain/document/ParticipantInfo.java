package com.ani.taku_backend.chatroom.domain.document;

import com.ani.taku_backend.chatroom.domain.constant.MarketRole;
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

    private MarketRole role;

    private Boolean isConnected;
    private Integer messageStock;
    private Instant lastDisconnectedAt;


    public ParticipantInfo() {
    }

    public ParticipantInfo(@Param("userId") Long userId, @Param("role") MarketRole role) {
        this.userId = userId;
        this.role = role;
        this.isConnected = true;
        this.lastDisconnectedAt = Instant.now();
        this.messageStock = INITIAL_MESSAGE_STOCK;
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
     * 참여자를 연결 상태로 설정합니다.
     */
    public void connect() {
        this.isConnected = true;
    }

    /**
     * 참여자를 연결 해제 상태로 설정하고 마지막 연결 해제 시간을 현재로 업데이트합니다.
     */
    public void disconnect() {
        this.isConnected = false;
        this.lastDisconnectedAt = Instant.now();
    }

    /**
     * 참여자가 판매자인지 확인합니다.
     * 
     * @return 판매자인 경우 true
     */
    public boolean isSeller() {
        return this.role == MarketRole.SELLER;
    }

    /**
     * 참여자가 구매자인지 확인합니다.
     * 
     * @return 구매자인 경우 true
     */
    public boolean isBuyer() {
        return this.role == MarketRole.BUYER;
    }


}