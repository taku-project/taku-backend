package com.ani.taku_backend.chatroom.dto.response;

import java.util.List;

public record ChatMessageListResponseDTO(
        List<ChatMessageResponseDTO> messages,
        boolean hasMore
) {
    public static ChatMessageListResponseDTO of(
            List<ChatMessageResponseDTO> messages,
            boolean hasMore
    ) {
        return new ChatMessageListResponseDTO(
                messages,
                hasMore
        );
    }
}
