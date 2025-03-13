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

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 채팅방 관련 엔티티와 DTO 간의 변환을 담당하는 매퍼
 */
@Component
@RequiredArgsConstructor
public class ChatRoomMapper {

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

        if (buyer == null || seller == null) {
            return null;
        }

        Long buyerId = buyer.getUserId();
        Long sellerId = seller.getUserId();
        
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
                .buyerNickname(buyer.getNickname())
                .sellerNickname(seller.getNickname())
                .buyerProfileImageUrl(buyer.getProfileImg())
                .sellerProfileImageUrl(seller.getProfileImg())
                .lastMessage(lastMessageDTO)
                .createdAt(chatRoom.getCreatedAt())
                .updatedAt(chatRoom.getUpdatedAt())
                .articleImageUrl(articleImageUrl)
                .unreadMessageCount(unreadCount)
                .build();
    }

    /**
     * 시간 형식을 변환합니다.
     */
    public String formatMessageTime(LocalDateTime time) {
        return ChatDateTimeFormatter.formatMessageTime(time);
    }
} 