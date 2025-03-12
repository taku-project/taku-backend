package com.ani.taku_backend.chatroom.util.mapper;

import com.ani.taku_backend.chatroom.domain.constant.ParticipantRole;
import com.ani.taku_backend.chatroom.domain.document.ChatMessage;
import com.ani.taku_backend.chatroom.domain.document.ChatRoomMetaInfo;
import com.ani.taku_backend.chatroom.domain.document.ParticipantInfo;
import com.ani.taku_backend.chatroom.domain.document.Participants;
import com.ani.taku_backend.chatroom.domain.dto.response.ChatRoomResponseDTO;
import com.ani.taku_backend.chatroom.domain.entity.ChatRoom;
import com.ani.taku_backend.user.model.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Map;
import java.util.Optional;

/**
 * 채팅방 관련 엔티티와 DTO 간의 매핑을 담당하는 클래스
 */
@Mapper(componentModel = "spring")
public interface ChatRoomMapper {

    String UNKNOWN_USER = "알 수 없음";

    @Mapping(target = "buyerNickname", source = ".", qualifiedByName = "getBuyerNickname")
    @Mapping(target = "sellerNickname", source = ".", qualifiedByName = "getSellerNickname")
    @Mapping(target = "buyerId", source = ".", qualifiedByName = "getBuyerId")
    @Mapping(target = "sellerId", source = ".", qualifiedByName = "getSellerId")
    @Mapping(target = "lastMessage", source = "lastMessageMap", qualifiedByName = "getLastMessageContent")
    @Mapping(target = "lastMessageTime", source = "lastMessageMap", qualifiedByName = "getLastMessageTime")
    @Mapping(target = "lastMessageSenderId", source = "lastMessageMap", qualifiedByName = "getLastMessageSenderId")
    @Mapping(target = "unreadCount", source = "unreadCountMap", qualifiedByName = "getUnreadCount")
    @Mapping(target = "articleThumbnailUrl", source = "articleImageMap", qualifiedByName = "getArticleThumbnailUrl")
    @Mapping(target = "buyerProfileImageUrl", ignore = true)
    @Mapping(target = "sellerProfileImageUrl", ignore = true)
    ChatRoomResponseDTO toChatRoomResponseDTO(
            ChatRoom chatRoom,
            ChatRoomMetaInfo chatRoomMetaInfo,
            Map<Long, User> userMap,
            Map<Long, ChatMessage> lastMessageMap,
            Map<Long, Integer> unreadCountMap,
            Map<Long, String> articleImageMap);

    @Mapping(target = "buyerNickname", expression = "java(buyer != null ? buyer.getNickname() : UNKNOWN_USER)")
    @Mapping(target = "sellerNickname", expression = "java(seller != null ? seller.getNickname() : UNKNOWN_USER)")
    @Mapping(target = "buyerId", expression = "java(buyer != null ? buyer.getUserId() : 0L)")
    @Mapping(target = "sellerId", expression = "java(seller != null ? seller.getUserId() : 0L)")
    @Mapping(target = "lastMessage", expression = "java(lastMessage.isPresent() ? lastMessage.get().getContent() : null)")
    @Mapping(target = "lastMessageTime", expression = "java(com.ani.taku_backend.chatroom.util.ChatDateTimeFormatter.formatMessageTime(lastMessage.isPresent() ? lastMessage.get().getSentAt() : null))")
    @Mapping(target = "lastMessageSenderId", expression = "java(lastMessage.isPresent() ? lastMessage.get().getSenderId() : null)")
    @Mapping(target = "buyerProfileImageUrl", expression = "java(buyer != null ? buyer.getProfileImg() : null)")
    @Mapping(target = "sellerProfileImageUrl", expression = "java(seller != null ? seller.getProfileImg() : null)")
    ChatRoomResponseDTO toChatRoomResponseDTOWithProfileImages(
            ChatRoom chatRoom,
            User buyer,
            User seller,
            Optional<ChatMessage> lastMessage,
            Integer unreadCount,
            String articleThumbnailUrl);
            
    // 헬퍼 메소드들
    @Named("getBuyerNickname")
    default String getBuyerNickname(ChatRoom chatRoom, ChatRoomMetaInfo metaInfo, Map<Long, User> userMap) {
        if (metaInfo == null || metaInfo.getParticipants() == null) {
            return UNKNOWN_USER;
        }
        
        Long buyerId = findBuyerId(metaInfo.getParticipants());
        User buyer = userMap.get(buyerId);
        return buyer != null ? buyer.getNickname() : UNKNOWN_USER;
    }
    
    @Named("getSellerNickname")
    default String getSellerNickname(ChatRoom chatRoom, ChatRoomMetaInfo metaInfo, Map<Long, User> userMap) {
        if (metaInfo == null || metaInfo.getParticipants() == null) {
            return UNKNOWN_USER;
        }
        
        Long sellerId = findSellerId(metaInfo.getParticipants());
        User seller = userMap.get(sellerId);
        return seller != null ? seller.getNickname() : UNKNOWN_USER;
    }
    
    @Named("getBuyerId")
    default Long getBuyerId(ChatRoom chatRoom, ChatRoomMetaInfo metaInfo, Map<Long, User> userMap) {
        if (metaInfo == null || metaInfo.getParticipants() == null) {
            return 0L;
        }
        return findBuyerId(metaInfo.getParticipants());
    }
    
    @Named("getSellerId")
    default Long getSellerId(ChatRoom chatRoom, ChatRoomMetaInfo metaInfo, Map<Long, User> userMap) {
        if (metaInfo == null || metaInfo.getParticipants() == null) {
            return 0L;
        }
        return findSellerId(metaInfo.getParticipants());
    }
    
    @Named("getLastMessageContent")
    default String getLastMessageContent(ChatRoom chatRoom, Map<Long, ChatMessage> lastMessageMap) {
        ChatMessage lastMessage = lastMessageMap.get(chatRoom.getId());
        return lastMessage != null ? lastMessage.getContent() : null;
    }
    
    @Named("getLastMessageTime")
    default String getLastMessageTime(ChatRoom chatRoom, Map<Long, ChatMessage> lastMessageMap) {
        ChatMessage lastMessage = lastMessageMap.get(chatRoom.getId());
        return com.ani.taku_backend.chatroom.util.ChatDateTimeFormatter.formatMessageTime(
                lastMessage != null ? lastMessage.getSentAt() : null);
    }
    
    @Named("getLastMessageSenderId")
    default Long getLastMessageSenderId(ChatRoom chatRoom, Map<Long, ChatMessage> lastMessageMap) {
        ChatMessage lastMessage = lastMessageMap.get(chatRoom.getId());
        return lastMessage != null ? lastMessage.getSenderId() : null;
    }
    
    @Named("getUnreadCount")
    default Integer getUnreadCount(ChatRoom chatRoom, Map<Long, Integer> unreadCountMap) {
        return unreadCountMap.getOrDefault(chatRoom.getId(), 0);
    }
    
    @Named("getArticleThumbnailUrl")
    default String getArticleThumbnailUrl(ChatRoom chatRoom, Map<Long, String> articleImageMap) {
        return articleImageMap.get(chatRoom.getArticleId());
    }
    
    // 유틸리티 메소드들
    default Long findBuyerId(Participants participants) {
        for (Long key : participants.getInfo().keySet()) {
            ParticipantInfo participantInfo = participants.getInfo().get(key);
            if (participantInfo.getRole() == ParticipantRole.BUYER) {
                return participantInfo.getUserId();
            }
        }
        return 0L;
    }
    
    default Long findSellerId(Participants participants) {
        for (Long key : participants.getInfo().keySet()) {
            ParticipantInfo participantInfo = participants.getInfo().get(key);
            if (participantInfo.getRole() == ParticipantRole.SELLER) {
                return participantInfo.getUserId();
            }
        }
        return 0L;
    }
} 