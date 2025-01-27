package com.ani.taku_backend.chatroom.model.constant;


public enum ChatRoomStatus {
    ACTIVE("활성"),
    INACTIVE("비활성");

    private final String description;

    ChatRoomStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}