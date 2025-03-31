package com.ani.taku_backend.chatroom.mapper;

import com.ani.taku_backend.chatroom.domain.document.ChatRoomMetaInfo;
import com.ani.taku_backend.chatroom.domain.entity.ChatRoom;
import com.ani.taku_backend.chatroom.domain.vo.ArticleImage;
import com.ani.taku_backend.chatroom.domain.vo.ChatRoomMessages;
import com.ani.taku_backend.chatroom.domain.vo.ChatRoomUsers;
import com.ani.taku_backend.chatroom.domain.vo.UnreadMessageCounts;
import com.ani.taku_backend.chatroom.dto.response.ChatMessageResponseDTO;
import com.ani.taku_backend.chatroom.dto.response.ChatRoomResponseDTO;
import com.ani.taku_backend.user.model.entity.User;
import org.springframework.stereotype.Component;

/**
 * 채팅방 도메인 객체를 DTO로 변환하는 컨버터 클래스
 */
@Component
public class ChatRoomDtoConverter {

    public ChatRoomResponseDTO toResponseDto(
            ChatRoom chatRoom,
            ChatRoomMetaInfo metaInfo,
            ChatRoomUsers users,
            ChatRoomMessages lastMessages,
            UnreadMessageCounts unreadCounts,
            ArticleImage articleImage,
            ChatMessageResponseDTO lastMessageDTO,
            String articleImageUrl,
            Integer unreadCount) {
        
        User buyer = chatRoom.getBuyer();
        User seller = chatRoom.getSeller();
        
        Long buyerId = buyer != null ? buyer.getUserId() : null;
        Long sellerId = seller != null ? seller.getUserId() : null;
        
        String buyerNickname = users.getUserNicknameOrUnknown(buyerId);
        String sellerNickname = users.getUserNicknameOrUnknown(sellerId);
        String buyerProfileImg = users.getUserProfileImage(buyerId);
        String sellerProfileImg = users.getUserProfileImage(sellerId);
        
        return ChatRoomResponseDTO.builder()
                .chatRoomId(chatRoom.getId())
                .wsRoomId(chatRoom.getWsRoomId())
                .articleId(chatRoom.getArticleId())
                .buyerId(buyerId)
                .sellerId(sellerId)
                .buyerNickname(buyerNickname)
                .sellerNickname(sellerNickname)
                .lastMessage(lastMessageDTO)
                .createdAt(chatRoom.getCreatedAt())
                .updatedAt(chatRoom.getUpdatedAt())
                .articleImageUrl(articleImageUrl)
                .unreadMessageCount(unreadCount)
                .buyerProfileImageUrl(buyerProfileImg)
                .sellerProfileImageUrl(sellerProfileImg)
                .build();
    }
}