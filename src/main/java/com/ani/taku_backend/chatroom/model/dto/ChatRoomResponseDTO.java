package com.ani.taku_backend.chatroom.model.dto;

import com.ani.taku_backend.chatroom.model.entity.ChatRoom;
import com.ani.taku_backend.user.model.entity.User;
import java.time.LocalDateTime;

public record ChatRoomResponseDTO(
        ChatRoomInfoDTO info,
        ParticipantsDTO participants,
        ChatRoomStatusDTO status
) {

    public record ChatRoomInfoDTO(
            Long id,
            String roomId,
            Long articleId,
            LocalDateTime createdAt
    ) {}

    public record ParticipantsDTO(
            ParticipantDTO buyer,
            ParticipantDTO seller
    ) {}

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

    public record ChatRoomStatusDTO(
            boolean canSendMessage,
            String exitMessage
    ) {}

    public static ChatRoomResponseDTO of(
            ChatRoom chatRoom,
            User buyer,
            User seller,
            Long currentUserId
    ) {
        return new ChatRoomResponseDTO(
                new ChatRoomInfoDTO(
                        chatRoom.getId(),
                        chatRoom.getRoomId(),
                        chatRoom.getArticleId(),
                        chatRoom.getCreatedAt()
                ),
                new ParticipantsDTO(
                        ParticipantDTO.from(buyer),
                        ParticipantDTO.from(seller)
                ),
                new ChatRoomStatusDTO(
                        chatRoom.canSendMessage(currentUserId),
                        chatRoom.getExitMessage()
                )
        );
    }
}