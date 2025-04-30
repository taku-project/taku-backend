package com.ani.taku_backend.chatroom.dto.response;

import com.ani.taku_backend.chatroom.domain.document.ChatMessage;
import com.ani.taku_backend.chatroom.util.ChatDateTimeFormatter;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record ChatMessageResDTO(
        String messageId,
        Long chatRoomId,
        String wsRoomId,
        Long senderId,
        String senderName,
        String content,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime sentAt,
        String formattedTime,
        Boolean read
) {
    public static ChatMessageResDTO from(ChatMessage message) {
        if (message == null) {
            return null;
        }
        
        return ChatMessageResDTO.builder()
                .messageId(message.getId())
                .chatRoomId(message.getChatRoomId())
                .senderId(message.getSenderId())
                .content(message.getContent())
                .sentAt(message.getSentAt())
                .formattedTime(ChatDateTimeFormatter.formatMessageTime(message.getSentAt()))
                .read(message.getRead())
                .build();
    }
    

    public static ChatMessageResDTO from(ChatMessage message, String senderName, String wsRoomId) {
        if (message == null) {
            return null;
        }
        
        return ChatMessageResDTO.builder()
                .messageId(message.getId())
                .chatRoomId(message.getChatRoomId())
                .wsRoomId(wsRoomId)
                .senderId(message.getSenderId())
                .senderName(senderName)
                .content(message.getContent())
                .sentAt(message.getSentAt())
                .formattedTime(ChatDateTimeFormatter.formatMessageTime(message.getSentAt()))
                .read(message.getRead())
                .build();
    }

}