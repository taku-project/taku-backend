package com.ani.taku_backend.chatroom.model.document;

import com.ani.taku_backend.chatroom.model.constant.ParticipantRole;
import com.ani.taku_backend.chatroom.model.entity.ChatRoom;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;
import java.time.Instant;

@Document(collection = "chat_room_meta")
@Getter
@Setter
public class ChatRoomMetaInfo {
    @Id
    private String id;
    private Long chatroomId;
    private Participants participants;

    private String lastMessageId;
    private Instant updateAt;

    private boolean isActive = true;

    @Builder
    public ChatRoomMetaInfo(Long chatroomId) {
        this.chatroomId = chatroomId;
        this.participants = new Participants();
        this.updateAt = Instant.now();
    }

    public void initializeParticipants(Long buyerId, Long sellerId) {
        this.participants.addParticipant(buyerId, ParticipantRole.BUYER);
        this.participants.addParticipant(sellerId, ParticipantRole.SELLER);
    }

    /*
    * 모든 참가자가 방을 나갔다면 방 상태를 비활성화하는 함수
    * 이때 CharRoom 도 같이 deactivate 해준다.
     */
    public void checkAndDeactivate() {
        if (participants.allParticipantsInactive()) {
            this.isActive = false;

        }
    }
}