package com.ani.taku_backend.chatroom.domain.constant;

/**
 * 장터 채팅에서 사용자의 역할을 정의하는 Enum 클래스입니다.
 * BUYER: 구매자 역할
 * SELLER: 판매자 역할
 */

public enum JangterChatRole {
    BUYER, 
    SELLER;
    
    /**
     * 현재 역할이 구매자인지 확인합니다.
     * 
     * @return 구매자인 경우 true
     */
    public boolean isBuyer() {
        return this == BUYER;
    }
    
    /**
     * 현재 역할이 판매자인지 확인합니다.
     * 
     * @return 판매자인 경우 true
     */
    public boolean isSeller() {
        return this == SELLER;
    }
}