package com.ani.taku_backend.chatroom.dto.response;

import com.ani.taku_backend.chatroom.domain.document.ChatMessage;
import com.ani.taku_backend.chatroom.util.ChatDateTimeFormatter;
import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record ChatMessageResponseDTO(
        String messageId,
        Long chatRoomId,
        String wsRoomId,
        String senderId,
        String senderName,
        String content,
        LocalDateTime sentAt,
        String formattedTime,
        Boolean read
) {
    public static ChatMessageResponseDTO from(ChatMessage message) {
        if (message == null) {
            return null;
        }
        
        return ChatMessageResponseDTO.builder()
                .messageId(message.getId())
                .chatRoomId(message.getChatRoomId())
                .senderId(String.valueOf(message.getSenderId()))
                .content(message.getContent())
                .sentAt(message.getSentAt())
                .formattedTime(ChatDateTimeFormatter.formatMessageTime(message.getSentAt()))
                .read(message.getRead())
                .build();
    }
    

    public static ChatMessageResponseDTO from(ChatMessage message, String senderName, String wsRoomId) {
        if (message == null) {
            return null;
        }
        
        return ChatMessageResponseDTO.builder()
                .messageId(message.getId())
                .chatRoomId(message.getChatRoomId())
                .wsRoomId(wsRoomId)
                .senderId(String.valueOf(message.getSenderId()))
                .senderName(senderName)
                .content(message.getContent())
                .sentAt(message.getSentAt())
                .formattedTime(ChatDateTimeFormatter.formatMessageTime(message.getSentAt()))
                .read(message.getRead())
                .build();
    }

}