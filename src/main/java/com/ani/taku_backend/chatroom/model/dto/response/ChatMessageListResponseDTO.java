package com.ani.taku_backend.chatroom.model.dto.response;

import java.util.List;

public record ChatMessageListResponseDTO(
        List<ChatMessageResponseDTO> messages,
        boolean hasMore,
        String oldestMessageId
) {
    public static ChatMessageListResponseDTO of(
            List<ChatMessageResponseDTO> messages,
            boolean hasMore,
            String oldestMessageId
    ) {
        return new ChatMessageListResponseDTO(
                messages,
                hasMore,
                oldestMessageId
        );
    }
}
