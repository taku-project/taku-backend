package com.ani.taku_backend.chatroom.model.document;

import lombok.Getter;
import java.time.Instant;

@Getter
public class ParticipantInfo {
    private String userId;
    private Boolean isConnected;
    private Integer messageStock;
    private Instant lastDisconnectedAt;

    public ParticipantInfo(String userId) {
        this.userId = userId;
        this.isConnected = false;
        this.messageStock = 0;
        this.lastDisconnectedAt = Instant.now();
    }

    public void plusMessage() {
        this.messageStock++;
    }

    public void resetMessageStock() {
        this.messageStock = 0;
    }
}