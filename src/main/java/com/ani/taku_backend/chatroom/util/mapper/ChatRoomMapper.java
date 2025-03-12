package com.ani.taku_backend.chatroom.util.mapper;

import com.ani.taku_backend.chatroom.domain.document.ChatMessage;
import com.ani.taku_backend.chatroom.domain.document.ChatRoomMetaInfo;
import com.ani.taku_backend.chatroom.domain.document.Participants;
import com.ani.taku_backend.chatroom.domain.dto.response.ChatRoomResponseDTO;
import com.ani.taku_backend.chatroom.domain.entity.ChatRoom;
import com.ani.taku_backend.chatroom.util.ChatDateTimeFormatter;
import com.ani.taku_backend.user.model.entity.User;
import org.mapstruct.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ChatRoomMapper {

    String UNKNOWN_USER = "알 수 없음";

    default ChatRoomResponseDTO toChatRoomResponseDTO(
            ChatRoom chatRoom,
            ChatRoomMetaInfo chatRoomMetaInfo,
            Map<Long, User> userMap,
            Map<Long, ChatMessage> lastMessageMap,
            Map<Long, Integer> unreadCountMap,
            Map<Long, String> articleImageMap) {
        
        if (chatRoom == null) {
            return null;
        }
        
        // 구매자/판매자 정보 찾기
        Long buyerId = 0L;
        Long sellerId = 0L;
        String buyerNickname = UNKNOWN_USER;
        String sellerNickname = UNKNOWN_USER;
        
        if (chatRoomMetaInfo != null && chatRoomMetaInfo.getParticipants() != null) {
            Participants participants = chatRoomMetaInfo.getParticipants();

            buyerId = participants.getBuyerId() != null ? participants.getBuyerId() : 0L;
            sellerId = participants.getSellerId() != null ? participants.getSellerId() : 0L;
            
            User buyer = userMap.get(buyerId);
            User seller = userMap.get(sellerId);
            
            buyerNickname = buyer != null ? buyer.getNickname() : UNKNOWN_USER;
            sellerNickname = seller != null ? seller.getNickname() : UNKNOWN_USER;
        }
        
        // 마지막 메시지 정보
        ChatMessage lastMessage = lastMessageMap.get(chatRoom.getId());
        String lastMessageContent = lastMessage != null ? lastMessage.getContent() : null;
        String lastMessageTime = formatMessageTime(lastMessage != null ? lastMessage.getSentAt() : null);
        Long lastMessageSenderId = lastMessage != null ? lastMessage.getSenderId() : null;
        
        // 안읽은 메시지 수
        Integer unreadCount = unreadCountMap.getOrDefault(chatRoom.getId(), 0);
        
        // 썸네일 URL
        String articleThumbnailUrl = articleImageMap.get(chatRoom.getArticleId());

        return new ChatRoomResponseDTO(
            chatRoom.getId(),
            chatRoom.getWsRoomId(),
            chatRoom.getArticleId(),
            buyerId,
            sellerId,
            chatRoom.getCreatedAt(),
            buyerNickname,
            sellerNickname,
            lastMessageContent,
            lastMessageTime,
            lastMessageSenderId,
            unreadCount,
            articleThumbnailUrl,
            null,
            null
        );
    }

    default ChatRoomResponseDTO toChatRoomResponseDTOWithProfileImages(
            ChatRoom chatRoom,
            User buyer,
            User seller,
            Optional<ChatMessage> lastMessage,
            Integer unreadCount,
            String articleThumbnailUrl) {
        
        if (chatRoom == null) {
            return null;
        }
        
        String buyerNickname = buyer != null ? buyer.getNickname() : UNKNOWN_USER;
        String sellerNickname = seller != null ? seller.getNickname() : UNKNOWN_USER;
        Long buyerId = buyer != null ? buyer.getUserId() : 0L;
        Long sellerId = seller != null ? seller.getUserId() : 0L;
        String buyerProfileImageUrl = buyer != null ? buyer.getProfileImg() : null;
        String sellerProfileImageUrl = seller != null ? seller.getProfileImg() : null;
        
        String lastMessageContent = null;
        String lastMessageTime = null;
        Long lastMessageSenderId = null;
        
        if (lastMessage.isPresent()) {
            ChatMessage message = lastMessage.get();
            lastMessageContent = message.getContent();
            lastMessageTime = formatMessageTime(message.getSentAt());
            lastMessageSenderId = message.getSenderId();
        }
        

        return new ChatRoomResponseDTO(
            chatRoom.getId(),
            chatRoom.getWsRoomId(),
            chatRoom.getArticleId(),
            buyerId,
            sellerId,
            chatRoom.getCreatedAt(),
            buyerNickname,
            sellerNickname,
            lastMessageContent,
            lastMessageTime,
            lastMessageSenderId,
            unreadCount,
            articleThumbnailUrl,
            buyerProfileImageUrl,
            sellerProfileImageUrl
        );
    }

    default String formatMessageTime(LocalDateTime time) {
        return ChatDateTimeFormatter.formatMessageTime(time);
    }

    @Mapping(target = "id", source = "id")
    @Mapping(target = "roomId", source = "wsRoomId")
    @Mapping(target = "articleId", source = "articleId")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "buyerId", constant = "0L")
    @Mapping(target = "sellerId", constant = "0L")
    @Mapping(target = "buyerNickname", constant = "알 수 없음")
    @Mapping(target = "sellerNickname", constant = "알 수 없음")
    @Mapping(target = "lastMessage", ignore = true)
    @Mapping(target = "lastMessageTime", ignore = true)
    @Mapping(target = "lastMessageSenderId", ignore = true)
    @Mapping(target = "unreadCount", constant = "0")
    @Mapping(target = "articleThumbnailUrl", ignore = true)
    @Mapping(target = "buyerProfileImageUrl", ignore = true)
    @Mapping(target = "sellerProfileImageUrl", ignore = true)
    ChatRoomResponseDTO chatRoomToBasicDTO(ChatRoom chatRoom);

    List<ChatRoomResponseDTO> chatRoomsToBasicDTOs(List<ChatRoom> chatRooms);
} 