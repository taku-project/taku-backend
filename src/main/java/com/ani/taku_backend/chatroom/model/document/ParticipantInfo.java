package com.ani.taku_backend.chatroom.model.document;

import com.ani.taku_backend.chatroom.model.constant.ParticipantRole;
import lombok.Getter;
import java.time.Instant;

@Getter
public class ParticipantInfo {
    private Long userId;

    private ParticipantRole role;

    private Boolean isConnected;
    private Integer messageStock;
    private Instant lastDisconnectedAt;

    public ParticipantInfo(Long userId, ParticipantRole role) {
        this.userId = userId;
        this.role = role;
        this.isConnected = true;
        this.lastDisconnectedAt = Instant.now();
    }

    public void plusMessage() {
        this.messageStock++;
    }

    public void resetMessageStock() {
        this.messageStock = 0;
    }

    public void setDisconnected() {
        this.isConnected = false;
        this.lastDisconnectedAt = Instant.now();
    }

}