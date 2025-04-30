package com.ani.taku_backend.chatroom.dto.response;

import java.util.List;

public record ChatMessageListResDTO(
        List<ChatMessageResDTO> messages,
        boolean hasMore
) {
    public static ChatMessageListResDTO of(
            List<ChatMessageResDTO> messages,
            boolean hasMore
    ) {
        return new ChatMessageListResDTO(
                messages,
                hasMore
        );
    }
}
