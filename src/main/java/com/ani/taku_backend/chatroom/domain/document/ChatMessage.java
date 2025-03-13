package com.ani.taku_backend.chatroom.domain.document;

import com.ani.taku_backend.chatroom.domain.constant.ChatRoomStatus;
import com.ani.taku_backend.chatroom.domain.dto.response.ChatMessageResponseDTO;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MongoDB에 저장되는 채팅 메세지를 나타냅니다.
 * 각 메세지는 채팅방, 상품, 발신자 정보와 전송 시간, 읽음 상태 등을 포함합니다.
 */

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

    /**
     * 이 메시지가 특정 사용자가 보낸 것인지 확인합니다.
     *
     * @param userId 확인할 사용자 ID
     * @return 해당 사용자가 보낸 메시지인 경우 true
     */
    public boolean isSentBy(Long userId) {
        return this.senderId.equals(userId);
    }

    /**
     * 메시지를 읽음 상태로 표시합니다.
     */
    public void markAsRead() {
        this.read = true;
    }

    public static List<ChatMessageResponseDTO> toResponseDTOList(List<ChatMessage> messages) {
        return messages.stream()
                .map(message -> ChatMessageResponseDTO.from(message))
                .collect(Collectors.toList());
    }
}
