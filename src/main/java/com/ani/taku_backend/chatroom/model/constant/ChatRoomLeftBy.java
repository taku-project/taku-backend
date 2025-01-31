package com.ani.taku_backend.chatroom.model.constant;

public enum ChatRoomLeftBy {
    NONE("없음", "아무도 나가지 않았습니다."),
    BUYER("구매자", "구매자가 채팅방을 나갔습니다."),
    SELLER("판매자", "판매자가 채팅방을 나갔습니다.");

    private final String status;
    private final String message;

    ChatRoomLeftBy(String status, String message) {
        this.status = status;
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
} 