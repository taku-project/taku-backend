package com.ani.taku_backend.chatroom.domain.vo;

public class ChatRoomSummary {
    private final Long chatRoomId;
    private final boolean isActive;

    public ChatRoomSummary(Long chatRoomId,
                          boolean isActive) {
        this.chatRoomId = chatRoomId;
        this.isActive = isActive;
    }

    public Long getChatRoomId() {
        return chatRoomId;
    }

    public boolean isActive() {
        return isActive;
    }

} 