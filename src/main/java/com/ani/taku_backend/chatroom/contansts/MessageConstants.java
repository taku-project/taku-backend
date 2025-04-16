package com.ani.taku_backend.chatroom.contansts;


public class MessageConstants {

    private MessageConstants() {
    }

    public static final String UNKNOWN_USER = "알 수 없음";
    
    // 입력 검증 메시지
    public static final String USER_ID_NOT_NULL = "사용자 ID는 null일 수 없습니다";
    public static final String CHAT_ROOM_ID_NOT_NULL = "채팅방 ID는 null일 수 없습니다";
    public static final String ARTICLE_ID_NOT_NULL = "상품 ID는 null일 수 없습니다";
    
    // 객체 Null 검증 메시지
    public static final String CHAT_ROOM_NOT_NULL = "Chat room cannot be null";
    public static final String META_INFO_NOT_NULL = "Meta info cannot be null";
    public static final String BUYER_NOT_NULL = "Buyer cannot be null";
    public static final String SELLER_NOT_NULL = "Seller cannot be null";
    public static final String PRODUCT_NOT_NULL = "Product cannot be null";
} 