package com.ani.taku_backend.chatroom.domain.dto.request;

/**
 * WebSocket을 통한 채팅 메시지 요청 DTO입니다.
 * 채팅 메시지 전송과 읽음 상태 업데이트에 사용됩니다.
 */
public record ChatMessageRequestDTO(
    String roomId,    // WebSocket 채팅방 ID
    Long senderId,    // 메시지 발신자 ID
    String content    // 메시지 내용 (읽음 상태 업데이트 시 null)
) {

    public static ChatMessageRequestDTO forReadStatus(String roomId, Long senderId) {
        return new ChatMessageRequestDTO(roomId, senderId, null);
    }
} 