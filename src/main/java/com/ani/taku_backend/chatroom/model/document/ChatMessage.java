package com.ani.taku_backend.chatroom.model.document;

import com.ani.taku_backend.chatroom.model.constant.ChatRoomStatus;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "chat_messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    @Id
    private String id;


    private Long chatRoomId; // (ChatRoom의 Id와 연결)

    private Long articleId; // 판매글 ID, ChatRoom에서 참조

    private Long senderId; // 보낸 사람 ID

    private String content; // 메시지 내용

    private LocalDateTime sentAt; // 전송 시간

    private Boolean read = false; // 읽음 여부

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ChatRoomStatus status = ChatRoomStatus.ACTIVE;


    public static ChatMessage of(Long roomId, Long articleId, Long senderId, String content) {
        return new ChatMessage(
                null, // MongoDB의 경우 ID는 자동 생성
                roomId,
                articleId,
                senderId,
                content,
                LocalDateTime.now(), // 전송 시간 자동 설정
                false, // 기본적으로 읽지 않음
                ChatRoomStatus.ACTIVE // 기본 상태
        );
    }
}
