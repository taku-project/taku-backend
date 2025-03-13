package com.ani.taku_backend.chatroom.util;

import com.ani.taku_backend.chatroom.domain.constant.MarketRole;

/**
 * 채팅방 참여자 관련 공통 유틸리티 클래스
 * MySQL(ChatRoomParticipant)과 MongoDB(ParticipantInfo) 모델 간의 중복 로직을 줄이기 위해 사용됩니다.
 */
public final class ParticipantUtils {
    
    // 인스턴스화 방지
    private ParticipantUtils() {
        throw new AssertionError("유틸리티 클래스는 인스턴스화할 수 없습니다.");
    }
    
    /**
     * 주어진 역할이 구매자인지 확인합니다.
     * 
     * @param role 확인할 역할
     * @return 구매자인 경우 true
     */
    public static boolean isBuyer(MarketRole role) {
        return role == MarketRole.BUYER;
    }
    
    /**
     * 주어진 역할이 판매자인지 확인합니다.
     * 
     * @param role 확인할 역할
     * @return 판매자인 경우 true
     */
    public static boolean isSeller(MarketRole role) {
        return role == MarketRole.SELLER;
    }
} 