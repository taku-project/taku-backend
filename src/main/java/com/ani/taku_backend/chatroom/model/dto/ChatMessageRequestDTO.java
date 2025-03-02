package com.ani.taku_backend.chatroom.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageRequestDTO {
    private String roomId;    // WebSocket 채팅방 ID
    private Long senderId;    // 메시지 발신자 ID
    private String content;   // 메시지 내용
} 