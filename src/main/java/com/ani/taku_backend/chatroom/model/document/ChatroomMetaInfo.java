package com.ani.taku_backend.chatroom.model.document;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;
import java.time.Instant;

@Document(collection = "chat_room_meta")
@Getter
public class ChatroomMetaInfo {
    @Id
    private String chatroomId;
    private Participants participants;
    private String lastMessage;
    private Instant updateAt;

    @Builder
    public ChatroomMetaInfo(String chatroomId) {
        this.chatroomId = chatroomId;
        this.participants = new Participants();
        this.updateAt = Instant.now();
    }

    public void initializeParticipants(Long buyerId, Long sellerId) {
        this.participants.addParticipant(buyerId.toString());
        this.participants.addParticipant(sellerId.toString());
    }
}