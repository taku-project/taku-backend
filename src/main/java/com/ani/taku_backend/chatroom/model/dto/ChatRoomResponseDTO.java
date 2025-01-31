package com.ani.taku_backend.chatroom.model.dto;

import com.ani.taku_backend.chatroom.model.entity.ChatRoom;
import com.ani.taku_backend.user.model.entity.User;
import java.time.LocalDateTime;

public record ChatRoomResponseDTO(
        Long id,
        String roomId,
        Long articleId,
        ParticipantDTO buyer,
        ParticipantDTO seller,
        LocalDateTime createdAt
) {
    public record ParticipantDTO(
            Long userId,
            String nickname,
            String profileImg
    ) {
        public static ParticipantDTO from(User user) {
            return new ParticipantDTO(
                    user.getUserId(),
                    user.getNickname(),
                    user.getProfileImg()
            );
        }
    }

    public static ChatRoomResponseDTO of(ChatRoom chatRoom, User buyer, User seller) {
        return new ChatRoomResponseDTO(
                chatRoom.getId(),
                chatRoom.getRoomId(),
                chatRoom.getArticleId(),
                ParticipantDTO.from(buyer),
                ParticipantDTO.from(seller),
                chatRoom.getCreatedAt()
        );
    }
}
