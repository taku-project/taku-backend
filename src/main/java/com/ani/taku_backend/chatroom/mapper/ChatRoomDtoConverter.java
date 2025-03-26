package com.ani.taku_backend.chatroom.mapper;

import com.ani.taku_backend.chatroom.contansts.MessageConstants;
import com.ani.taku_backend.chatroom.domain.document.ChatMessage;
import com.ani.taku_backend.chatroom.domain.document.ChatRoomMetaInfo;
import com.ani.taku_backend.chatroom.domain.entity.ChatRoom;
import com.ani.taku_backend.chatroom.domain.vo.ArticleImage;
import com.ani.taku_backend.chatroom.domain.vo.ChatRoomMessages;
import com.ani.taku_backend.chatroom.domain.vo.ChatRoomUsers;
import com.ani.taku_backend.chatroom.domain.vo.UnreadMessageCounts;
import com.ani.taku_backend.chatroom.dto.response.ChatMessageResponseDTO;
import com.ani.taku_backend.chatroom.dto.response.ChatRoomResponseDTO;
import com.ani.taku_backend.user.model.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 채팅방 도메인 객체를 DTO로 변환하는 컨버터 클래스
 * 복잡한 매핑 로직을 캡슐화하여 DTO에서 비즈니스 로직을 분리합니다.
 */
@Slf4j
@Component
public class ChatRoomDtoConverter {

    /**
     * 채팅방과 관련 정보로부터 응답 DTO를 생성합니다.
     */
    public ChatRoomResponseDTO toResponseDto(
            ChatRoom chatRoom,
            ChatRoomMetaInfo metaInfo,
            ChatRoomUsers users,
            ChatRoomMessages lastMessages,
            UnreadMessageCounts unreadCounts,
            ArticleImage articleImage) {

        if (chatRoom == null || metaInfo == null || !chatRoom.isValid()) {
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

        // 참여자 정보 처리
        String buyerNickname;
        String sellerNickname;
        String buyerProfileImg;
        String sellerProfileImg;

        if (users != null) {
            buyerNickname = buyerId != null ? users.getUserNicknameOrUnknown(buyerId) : MessageConstants.UNKNOWN_USER;
            sellerNickname = sellerId != null ? users.getUserNicknameOrUnknown(sellerId) : MessageConstants.UNKNOWN_USER;
            buyerProfileImg = buyerId != null ? users.getUserProfileImage(buyerId) : null;
            sellerProfileImg = sellerId != null ? users.getUserProfileImage(sellerId) : null;
        } else {
            buyerNickname = buyer != null ? buyer.getNickname() : MessageConstants.UNKNOWN_USER;
            sellerNickname = seller != null ? seller.getNickname() : MessageConstants.UNKNOWN_USER;
            buyerProfileImg = buyer != null ? buyer.getProfileImg() : null;
            sellerProfileImg = seller != null ? seller.getProfileImg() : null;
        }

        // 마지막 메시지 처리
        ChatMessageResponseDTO lastMessageDTO = createLastMessageDTO(chatRoom, chatRoomId, lastMessages, metaInfo, users);

        // 아티클 이미지 URL 처리
        String articleImageUrl = getArticleImageUrl(chatRoom.getArticleId(), articleImage);

        // 읽지 않은 메시지 수 처리
        Integer unreadCount = getUnreadCount(chatRoomId, unreadCounts);

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

    /**
     * 마지막 메시지 DTO를 생성합니다.
     */
    private ChatMessageResponseDTO createLastMessageDTO(
            ChatRoom chatRoom,
            Long chatRoomId,
            ChatRoomMessages lastMessages,
            ChatRoomMetaInfo metaInfo,
            ChatRoomUsers users) {

        if (chatRoomId == null) {
            return null;
        }

        Optional<ChatMessage> messageOpt;
        try {
            messageOpt = lastMessages != null
                    ? lastMessages.getLastMessage(chatRoomId)
                    : Optional.ofNullable(metaInfo != null ? metaInfo.getLastMessage() : null);
        } catch (Exception e) {
            log.warn("마지막 메시지 조회 중 오류 발생: chatRoomId={}, error={}", chatRoomId, e.getMessage());
            return null;
        }

        if (messageOpt.isEmpty()) {
            return null;
        }

        ChatMessage lastMessage = messageOpt.get();
        if (lastMessage == null || lastMessage.getSenderId() == null) {
            return null;
        }

        String senderName = users != null
                ? users.getUserNicknameOrUnknown(lastMessage.getSenderId())
                : MessageConstants.UNKNOWN_USER;

        try {
            return ChatMessageResponseDTO.from(lastMessage, senderName, chatRoom.getWsRoomId());
        } catch (Exception e) {
            log.warn("메시지 DTO 생성 중 오류 발생: chatRoomId={}, error={}", chatRoomId, e.getMessage());
            return null;
        }
    }

    /**
     * 상품 이미지 URL을 가져옵니다.
     */
    private String getArticleImageUrl(Long articleId, ArticleImage articleImage) {
        if (articleId == null || articleImage == null) {
            return null;
        }
        try {
            return articleImage.getImageUrl(articleId);
        } catch (Exception e) {
            log.warn("상품 이미지 조회 중 오류 발생: articleId={}, error={}", articleId, e.getMessage());
            return null;
        }
    }

    /**
     * 읽지 않은 메시지 수를 가져옵니다.
     */
    private Integer getUnreadCount(Long chatRoomId, UnreadMessageCounts unreadCounts) {
        return unreadCounts != null ? unreadCounts.getUnreadCount(chatRoomId) : 0;
    }
}