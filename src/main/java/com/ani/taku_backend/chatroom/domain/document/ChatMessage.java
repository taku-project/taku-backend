package com.ani.taku_backend.chatroom.domain.document;

import com.ani.taku_backend.chatroom.domain.constant.ChatRoomStatus;
import com.ani.taku_backend.chatroom.dto.response.ChatMessageResDTO;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * MongoDB에 저장되는 채팅 메세지를 나타냅니다.
 * 각 메세지는 채팅방, 상품, 발신자 정보와 전송 시간, 읽음 상태 등을 포함합니다.
 */

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

    /**
     * 채팅 메시지를 생성합니다.
     * 
     * 참고: 메시지에 명시적으로 UUID를 할당하는 이유
     * 1. 채팅 메시지는 ChatRoomMetaInfo 내부에 임베디드 문서로 저장됨
     * 2. MongoDB는 임베디드 문서에 자동으로 ID를 생성하지 않음 (최상위 문서만 자동 생성)
     * 3. 모든 메시지에 고유 ID를 보장하기 위해 UUID 사용
     */
    public static ChatMessage of(Long roomId, Long articleId, Long senderId, String content) {
        return new ChatMessage(
                UUID.randomUUID().toString(), // 명시적으로 UUID 생성하여 메시지 ID 할당
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
     * 메시지를 읽음 상태로 표시합니다.
     */
    public void markAsRead() {
        this.read = true;
    }

    public static List<ChatMessageResDTO> toResponseDTOList(List<ChatMessage> messages) {
        return messages.stream()
                .map(ChatMessageResDTO::from)
                .collect(Collectors.toList());
    }
}
