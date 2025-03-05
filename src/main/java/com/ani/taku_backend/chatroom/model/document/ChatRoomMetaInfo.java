package com.ani.taku_backend.chatroom.model.document;

import com.ani.taku_backend.chatroom.model.constant.ParticipantRole;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

/**
 * 채팅방의 메타 정보를 관리하는 클래스입니다.
 * 참가자 정보, 마지막 메세지 ID, 업데이트 시간 및 활성 상태 등을 포함합니다.
 */

@Document(collection = "chat_room_meta")
@Getter
@Setter
public class ChatRoomMetaInfo {
    @Id
    private String id;
    @Field("chatRoomId")
    private Long chatRoomId;
    private Participants participants;

    private String lastMessageId;
    private Instant updateAt;

    private boolean isActive = true;

    public ChatRoomMetaInfo() {
    }

    /**
     * 지정된 채팅방 ID로 메타 정보를 초기화합니다.
     *
     * @param chatRoomId 채팅방의 고유 ID
     */
    @Builder
    public ChatRoomMetaInfo(@Param("chatRoomId") Long chatRoomId) {
        this.chatRoomId = chatRoomId;
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