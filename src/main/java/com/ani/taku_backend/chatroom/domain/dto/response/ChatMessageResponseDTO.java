package com.ani.taku_backend.chatroom.domain.dto.response;

import com.ani.taku_backend.chatroom.domain.document.ChatMessage;
import com.ani.taku_backend.chatroom.util.ChatDateTimeFormatter;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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

    public static List<ChatMessageResponseDTO> listFrom(List<ChatMessage> messages) {
        return messages.stream()
                .map(ChatMessageResponseDTO::from)
                .collect(Collectors.toList());
    }
}