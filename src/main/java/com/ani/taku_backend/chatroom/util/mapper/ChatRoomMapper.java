package com.ani.taku_backend.chatroom.util.mapper;

import com.ani.taku_backend.chatroom.domain.constant.ParticipantRole;
import com.ani.taku_backend.chatroom.domain.document.ChatMessage;
import com.ani.taku_backend.chatroom.domain.document.ChatRoomMetaInfo;
import com.ani.taku_backend.chatroom.domain.document.ParticipantInfo;
import com.ani.taku_backend.chatroom.domain.document.Participants;
import com.ani.taku_backend.chatroom.domain.dto.response.ChatRoomResponseDTO;
import com.ani.taku_backend.chatroom.domain.entity.ChatRoom;
import com.ani.taku_backend.user.model.entity.User;

import java.util.Map;

/**
 * 채팅방 관련 엔티티와 DTO 간의 매핑을 담당하는 클래스
 */
public class ChatRoomMapper {

    private static final String UNKNOWN_USER = "알 수 없음";

    /**
     * 채팅방 정보를 DTO로 변환합니다.
     */
    public static ChatRoomResponseDTO toChatRoomResponseDTO(
            ChatRoom chatRoom,
            ChatRoomMetaInfo chatRoomMetaInfo,
            Map<Long, User> userMap,
            Map<Long, ChatMessage> lastMessageMap,
            Map<Long, Integer> unreadCountMap,
            Map<Long, String> articleImageMap) {
        
        if (chatRoomMetaInfo == null || chatRoomMetaInfo.getParticipants() == null || chatRoomMetaInfo.getParticipants().getInfo() == null) {
            return null;
        }

        Participants participants = chatRoomMetaInfo.getParticipants();

        Long buyerId = 0L;
        Long sellerId = 0L;

        for(Long key : participants.getInfo().keySet()) {
            ParticipantInfo participantInfo = participants.getInfo().get(key);
            if(participantInfo.getRole() == ParticipantRole.BUYER) {
                buyerId = participantInfo.getUserId();
            } else {
                sellerId = participantInfo.getUserId();
            }
        }

        // 구매자, 판매자 정보 가져오기
        User buyer = userMap.get(buyerId);
        User seller = userMap.get(sellerId);

        String buyerNickname = buyer != null ? buyer.getNickname() : UNKNOWN_USER;
        String sellerNickname = seller != null ? seller.getNickname() : UNKNOWN_USER;

        // 마지막 메시지 가져오기
        ChatMessage lastMessage = lastMessageMap.get(chatRoom.getId());

        // 안읽은 메시지 개수 가져오기
        Integer unreadCount = unreadCountMap.get(chatRoom.getId());

        // 상품 이미지 가져오기
        String articleThumbnailUrl = articleImageMap.get(chatRoom.getArticleId());

        return ChatRoomResponseDTO.of(
                chatRoom,
                buyerId,
                sellerId,
                buyerNickname,
                sellerNickname,
                lastMessage,
                unreadCount,
                articleThumbnailUrl
        );
    }
} 