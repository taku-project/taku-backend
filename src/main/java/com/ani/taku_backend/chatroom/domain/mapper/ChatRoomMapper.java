package com.ani.taku_backend.chatroom.domain.mapper;

import com.ani.taku_backend.chatroom.domain.document.ChatMessage;
import com.ani.taku_backend.chatroom.domain.document.ChatRoomMetaInfo;
import com.ani.taku_backend.chatroom.domain.dto.response.ChatMessageResponseDTO;
import com.ani.taku_backend.chatroom.domain.dto.response.ChatRoomResponseDTO;
import com.ani.taku_backend.chatroom.domain.entity.ChatRoom;
import com.ani.taku_backend.chatroom.util.ChatDateTimeFormatter;
import com.ani.taku_backend.user.model.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 채팅방 관련 엔티티와 DTO 간의 변환을 담당하는 매퍼
 */
@Component
@RequiredArgsConstructor
public class ChatRoomMapper {

    private static final Logger log = LoggerFactory.getLogger(ChatRoomMapper.class);

    public static final String UNKNOWN_USER = "알 수 없음";

    /**
     * 채팅방 정보와 부가 정보들을 결합하여 응답 DTO로 변환합니다.
     */
    public ChatRoomResponseDTO toChatRoomResponseDTO(
            ChatRoom chatRoom,
            ChatRoomMetaInfo chatRoomMetaInfo,
            Map<Long, User> userMap,
            Map<Long, ChatMessage> lastMessageMap,
            Map<Long, Integer> unreadCountMap,
            Map<Long, String> articleImageMap) {
        
        if (chatRoom == null || chatRoomMetaInfo == null) {
            return null;
        }
        
        Long chatRoomId = chatRoom.getId();
        User buyer = chatRoom.getBuyer();
        User seller = chatRoom.getSeller();

        if (buyer == null && seller == null) {
            return null;
        }

        Long buyerId = buyer != null ? buyer.getUserId() : null;
        Long sellerId = seller != null ? seller.getUserId() : null;
        
        String buyerNickname = buyer != null ? buyer.getNickname() : UNKNOWN_USER;
        String sellerNickname = seller != null ? seller.getNickname() : UNKNOWN_USER;
        
        String buyerProfileImg = buyer != null ? buyer.getProfileImg() : null;
        String sellerProfileImg = seller != null ? seller.getProfileImg() : null;
        
        // 마지막 메시지 처리
        ChatMessage lastMessage = lastMessageMap.get(chatRoomId);
        ChatMessageResponseDTO lastMessageDTO = null;
        
        if (lastMessage != null) {
            // 메시지 발신자 정보 확인
            User sender = userMap.get(lastMessage.getSenderId());
            String senderName = sender != null ? sender.getNickname() : UNKNOWN_USER;
            
            lastMessageDTO = ChatMessageResponseDTO.builder()
                    .messageId(lastMessage.getId())
                    .chatRoomId(lastMessage.getChatRoomId())
                    .wsRoomId(chatRoom.getWsRoomId())
                    .senderId(String.valueOf(lastMessage.getSenderId()))
                    .senderName(senderName)
                    .content(lastMessage.getContent())
                    .sentAt(lastMessage.getSentAt())
                    .formattedTime(ChatDateTimeFormatter.formatMessageTime(lastMessage.getSentAt()))
                    .read(lastMessage.getRead())
                    .build();
        }

        // 아티클 이미지
        String articleImageUrl = articleImageMap.getOrDefault(chatRoom.getArticleId(), null);
        
        // 안읽은 메시지 수
        Integer unreadCount = unreadCountMap.getOrDefault(chatRoomId, 0);

        return ChatRoomResponseDTO.builder()
                .chatRoomId(chatRoom.getId())
                .wsRoomId(chatRoom.getWsRoomId())
                .articleId(chatRoom.getArticleId())
                .buyerId(buyerId)
                .sellerId(sellerId)
                .buyerNickname(buyerNickname)
                .sellerNickname(sellerNickname)
                .buyerProfileImageUrl(buyerProfileImg)
                .sellerProfileImageUrl(sellerProfileImg)
                .lastMessage(lastMessageDTO)
                .createdAt(chatRoom.getCreatedAt())
                .updatedAt(chatRoom.getUpdatedAt())
                .articleImageUrl(articleImageUrl)
                .unreadMessageCount(unreadCount)
                .build();
    }


} 