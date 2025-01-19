package com.ani.taku_backend.chatroom.model.document;

import lombok.Getter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Getter
public class Participants {
    private Map<String, ParticipantInfo> info = new ConcurrentHashMap<>();

    public void addParticipant(String userId) {
        info.put(userId, new ParticipantInfo(userId));
    }
}