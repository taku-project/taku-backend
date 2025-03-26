package com.ani.taku_backend.chatroom.dto.response;

import com.ani.taku_backend.chatroom.domain.entity.ChatRoom;
import com.ani.taku_backend.chatroom.domain.vo.ArticleImage;
import com.ani.taku_backend.chatroom.domain.vo.ChatRoomMessages;
import com.ani.taku_backend.chatroom.domain.vo.ChatRoomMetaInfos;
import com.ani.taku_backend.chatroom.domain.vo.ChatRoomUsers;
import com.ani.taku_backend.chatroom.domain.vo.UnreadMessageCounts;
import com.ani.taku_backend.chatroom.mapper.ChatRoomDtoConverter;
import lombok.Value;

import java.util.List;

/**
 * 채팅방 관련 데이터를 함께 전달하기 위한 DTO 클래스입니다.
 * 채팅방 목록 조회 시 여러 VO들을 묶어 전달하는 용도로 사용됩니다.
 */
@Value
public class ChatRoomCompositeDTO {
    ChatRoomMetaInfos metaInfos;
    ArticleImage articleImage;
    ChatRoomMessages lastMessages;
    UnreadMessageCounts unreadCounts;
    ChatRoomUsers users;
    ChatRoomDtoConverter dtoConverter;

    public ChatRoomCompositeDTO(
            ChatRoomMetaInfos metaInfos,
            ArticleImage articleImage,
            ChatRoomMessages lastMessages,
            UnreadMessageCounts unreadCounts,
            ChatRoomUsers users,
            ChatRoomDtoConverter dtoConverter) {
        this.metaInfos = metaInfos;
        this.articleImage = articleImage;
        this.lastMessages = lastMessages;
        this.unreadCounts = unreadCounts;
        this.users = users;
        this.dtoConverter = dtoConverter;
    }

    public List<ChatRoomResponseDTO> toChatRoomResponseDTOs(List<ChatRoom> chatRooms) {
        return chatRooms.stream()
                .map(this::toChatRoomResponseDTO)
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toList());
    }
    
    public ChatRoomResponseDTO toChatRoomResponseDTO(ChatRoom room) {
        return metaInfos.getMetaInfo(room.getId())
                .map(metaInfo -> dtoConverter.toResponseDto(
                        room,
                        metaInfo,
                        users,
                        lastMessages,
                        unreadCounts,
                        articleImage
                ))
                .orElse(null);
    }
} 