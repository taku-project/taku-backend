package com.ani.taku_backend.chatroom.model.dto.response;

import com.ani.taku_backend.chatroom.model.document.ChatMessage;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record ChatMessageResponseDTO(
        String id,
        Long chatRoomId,
        Long senderId,
        String content,
        LocalDateTime sentAt,
        Boolean read
) {
    public static ChatMessageResponseDTO from(ChatMessage message) {
        return new ChatMessageResponseDTO(
                message.getId(),
                message.getChatRoomId(),
                message.getSenderId(),
                message.getContent(),
                message.getSentAt(),
                message.getRead()
        );
    }

    public static List<ChatMessageResponseDTO> listFrom(List<ChatMessage> messages) {
        return messages.stream()
                .map(ChatMessageResponseDTO::from)
                .collect(Collectors.toList());
    }
}