package com.ani.taku_backend.chatroom.model.dto;

import com.ani.taku_backend.chatroom.model.entity.ChatRoom;
import java.time.LocalDateTime;

public record ChatRoomResponseDTO(
        Long id,
        String roomId,
        Long articleId,
        Long buyerId,
        Long sellerId,
        LocalDateTime createdAt
) {
    /**
     * ChatRoom 엔티티와 관련 식별자를 기반으로 DTO를 생성
     *
     * @param chatRoom 채팅방 엔티티
     * @param buyerId 구매자 ID
     * @param sellerId 판매자 ID
     * @return 생성된 ChatRoomResponseDTO 객체
     */
    public static ChatRoomResponseDTO of(ChatRoom chatRoom, Long buyerId, Long sellerId) {
        return new ChatRoomResponseDTO(
                chatRoom.getId(),
                chatRoom.getWsRoomId(),
                chatRoom.getArticleId(),
                buyerId,
                sellerId,
                chatRoom.getCreatedAt()
        );
    }
}