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
import com.ani.taku_backend.jangter.model.entity.DuckuJangter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * 채팅방 도메인 엔티티와 DTO 간의 변환을 담당하는 매퍼 클래스
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatRoomDtoConverter {

    /**
     * 채팅방 엔티티와 관련 정보를 DTO로 변환합니다.
     * 상품 정보(DuckuJangter)를 포함하여 변환합니다.
     */
    public ChatRoomResponseDTO toChatRoomResponseDTO(
            ChatRoom chatRoom,
            ChatRoomMetaInfo metaInfo,
            ChatRoomUsers users,
            ChatRoomMessages lastMessages,
            UnreadMessageCounts unreadCounts,
            ArticleImage articleImage,
            DuckuJangter article) {
        
        if (chatRoom == null || metaInfo == null || !chatRoom.isValid()) {
            return null;
        }
        
        // 상품 정보 추출
        String articleName = null;
        BigDecimal articlePrice = null;
        if (article != null) {
            articleName = article.getTitle();
            articlePrice = article.getPrice();
        }
        
        // 사용자 정보 추출
        Long buyerId = chatRoom.getBuyer() != null ? chatRoom.getBuyer().getUserId() : null;
        Long sellerId = chatRoom.getSeller() != null ? chatRoom.getSeller().getUserId() : null;
        
        String buyerNickname = users.getUserNicknameOrUnknown(buyerId);
        String sellerNickname = users.getUserNicknameOrUnknown(sellerId);
        
        String buyerProfileImageUrl = users.getUserProfileImage(buyerId);
        String sellerProfileImageUrl = users.getUserProfileImage(sellerId);
        
        // 마지막 메시지 정보
        ChatMessageResponseDTO lastMessageDTO = createLastMessageResponseDTO(
                chatRoom.getId(), lastMessages, chatRoom.getWsRoomId(), users);
        
        // 상품 이미지 URL
        String articleImageUrl = null;
        if (articleImage != null) {
            try {
                articleImageUrl = articleImage.getImageUrl(chatRoom.getArticleId());
            } catch (Exception e) {
                // 실제 예외가 발생한 경우만 WARN 로그 남김
                if (!(e instanceof NoSuchElementException || e instanceof NullPointerException)) {
                    log.warn("상품 이미지 조회 중 오류 발생: articleId={}, error={}", 
                            chatRoom.getArticleId(), e.getMessage());
                } else {
                    // 이미지가 없는 경우는 디버그 로그만
                    log.debug("상품 이미지 없음: articleId={}", chatRoom.getArticleId());
                }
            }
        }
        
        // 읽지 않은 메시지 수
        Integer unreadMessageCount = unreadCounts.getUnreadCount(chatRoom.getId());
        
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
                .unreadMessageCount(unreadMessageCount)
                .buyerProfileImageUrl(buyerProfileImageUrl)
                .sellerProfileImageUrl(sellerProfileImageUrl)
                .articleName(articleName)
                .articlePrice(articlePrice)
                .build();
    }
    
    /**
     * 채팅 메시지를 DTO로 변환합니다.
     */
    private ChatMessageResponseDTO createLastMessageResponseDTO(
            Long chatRoomId, 
            ChatRoomMessages lastMessages, 
            String wsRoomId,
            ChatRoomUsers users) {
        
        if (chatRoomId == null || lastMessages == null) {
            return null;
        }
        
        try {
            Optional<ChatMessage> messageOpt = lastMessages.getLastMessage(chatRoomId);
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
            
            return ChatMessageResponseDTO.from(lastMessage, senderName, wsRoomId);
        } catch (Exception e) {
            log.warn("마지막 메시지 DTO 변환 중 오류 발생: chatRoomId={}, error={}", chatRoomId, e.getMessage());
            return null;
        }
    }

}