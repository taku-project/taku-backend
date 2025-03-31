package com.ani.taku_backend.chatroom.dto.response;

import com.ani.taku_backend.chatroom.domain.entity.ChatRoom;
import com.ani.taku_backend.chatroom.domain.vo.ArticleImage;
import com.ani.taku_backend.chatroom.domain.vo.ChatRoomMessages;
import com.ani.taku_backend.chatroom.domain.vo.ChatRoomMetaInfos;
import com.ani.taku_backend.chatroom.domain.vo.ChatRoomUsers;
import com.ani.taku_backend.chatroom.domain.vo.UnreadMessageCounts;
import com.ani.taku_backend.chatroom.service.query.ChatRoomQueryService;
import com.ani.taku_backend.jangter.model.entity.DuckuJangter;
import lombok.Value;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

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
    ChatRoomQueryService chatRoomQueryService;

    /**
     * 호환성을 위한 생성자 - 서비스 참조를 받습니다.
     */
    public ChatRoomCompositeDTO(
            ChatRoomMetaInfos metaInfos,
            ArticleImage articleImage,
            ChatRoomMessages lastMessages,
            UnreadMessageCounts unreadCounts,
            ChatRoomUsers users,
            ChatRoomQueryService chatRoomQueryService) {
        this.metaInfos = metaInfos;
        this.articleImage = articleImage;
        this.lastMessages = lastMessages;
        this.unreadCounts = unreadCounts;
        this.users = users;
        this.chatRoomQueryService = chatRoomQueryService;
    }

    /**
     * 기존 호환성을 유지하면서도 DTO 변환 메서드를 개선합니다.
     * 추가로 상품 정보를 포함할 수 있습니다.
     */
    public ChatRoomResponseDTO toChatRoomResponseDto(ChatRoom room, DuckuJangter article) {
        if (room == null || !isValidRoom(room)) {
            return null;
        }
        
        return metaInfos.getMetaInfo(room.getId())
                .map(metaInfo -> {
                    if (chatRoomQueryService != null) {
                        // 호환성을 위해 기존 메서드 호출
                        ChatRoomResponseDTO dto = chatRoomQueryService.createChatRoomResponseDTO(
                                room,
                                metaInfo,
                                users,
                                lastMessages,
                                unreadCounts,
                                articleImage
                        );
                        
                        // 상품 정보 추가
                        if (article != null && dto != null) {
                            dto = dto.toBuilder()
                                    .articleName(article.getTitle())
                                    .articlePrice(article.getPrice())
                                    .build();
                        }
                        return dto;
                    } else {
                        // chatRoomQueryService가 null인 경우를 대비한 처리
                        return null;
                    }
                })
                .orElse(null);
    }

    /**
     * 채팅방 목록을 DTO 목록으로 변환합니다.
     */
    public List<ChatRoomResponseDTO> toChatRoomResponseDtos(List<ChatRoom> chatRooms) {
        if (chatRooms == null || chatRooms.isEmpty()) {
            return List.of();
        }
        
        return chatRooms.stream()
                .map(room -> toChatRoomResponseDto(room, null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    
    /**
     * 채팅방 유효성 검사
     */
    private boolean isValidRoom(ChatRoom room) {
        return room.getId() != null && room.getArticleId() != null;
    }
} 