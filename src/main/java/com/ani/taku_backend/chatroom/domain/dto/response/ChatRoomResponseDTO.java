package com.ani.taku_backend.chatroom.domain.dto.response;

import com.ani.taku_backend.chatroom.domain.document.ChatMessage;
import com.ani.taku_backend.chatroom.domain.document.ChatRoomMetaInfo;
import com.ani.taku_backend.chatroom.domain.entity.ChatRoom;
import com.ani.taku_backend.chatroom.domain.vo.ArticleImages;
import com.ani.taku_backend.chatroom.domain.vo.ChatRoomMessages;
import com.ani.taku_backend.chatroom.domain.vo.ChatRoomUsers;
import com.ani.taku_backend.chatroom.domain.vo.UnreadMessageCounts;
import com.ani.taku_backend.chatroom.util.ChatDateTimeFormatter;
import com.ani.taku_backend.user.model.entity.User;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 채팅방 정보를 응답하기 위한 DTO 클래스
 */
@Getter
@ToString
public class ChatRoomResponseDTO {

    private final Long chatRoomId;
    private final String wsRoomId;
    private final Long articleId;
    private final Long buyerId;
    private final Long sellerId;
    private final String buyerNickname;
    private final String sellerNickname;
    private final ChatMessageResponseDTO lastMessage;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final String articleImageUrl;
    private final Integer unreadMessageCount;
    private final String buyerProfileImageUrl;
    private final String sellerProfileImageUrl;

    @Builder
    private ChatRoomResponseDTO(
            Long chatRoomId,
            String wsRoomId,
            Long articleId,
            Long buyerId,
            Long sellerId,
            String buyerNickname,
            String sellerNickname,
            ChatMessageResponseDTO lastMessage,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            String articleImageUrl,
            Integer unreadMessageCount,
            String buyerProfileImageUrl,
            String sellerProfileImageUrl) {
        this.chatRoomId = chatRoomId;
        this.wsRoomId = wsRoomId;
        this.articleId = articleId;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.buyerNickname = buyerNickname;
        this.sellerNickname = sellerNickname;
        this.lastMessage = lastMessage;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.articleImageUrl = articleImageUrl;
        this.unreadMessageCount = unreadMessageCount;
        this.buyerProfileImageUrl = buyerProfileImageUrl;
        this.sellerProfileImageUrl = sellerProfileImageUrl;
    }

    /**
     * 채팅방과 관련 정보로부터 응답 DTO를 생성합니다.
     * 단일 채팅방에 대한 DTO 생성을 위한 정적 팩토리 메서드입니다.
     */
    public static ChatRoomResponseDTO from(
            ChatRoom chatRoom,
            ChatRoomMetaInfo metaInfo,
            ChatRoomUsers users,
            ChatRoomMessages lastMessages,
            UnreadMessageCounts unreadCounts,
            ArticleImages articleImages) {
        
        if (chatRoom == null || metaInfo == null || !chatRoom.isValid()) {
            return null;
        }
        
        return new ResponseDTOBuilder(chatRoom)
                .withMetaInfo(metaInfo)
                .withUsers(users)
                .withLastMessages(lastMessages)
                .withUnreadCounts(unreadCounts)
                .withArticleImages(articleImages)
                .build();
    }
    
    /**
     * ChatRoomResponseDTO를 생성하기 위한 빌더 클래스
     * 내부 클래스로 캡슐화하여 빌더 패턴의 사용을 제한합니다.
     */
    private static class ResponseDTOBuilder {
        
        private static final String UNKNOWN_USER = "알 수 없음";
        
        private final ChatRoom chatRoom;
        private ChatRoomMetaInfo metaInfo;
        private ChatRoomUsers users;
        private ChatRoomMessages lastMessages;
        private UnreadMessageCounts unreadCounts;
        private ArticleImages articleImages;
        
        private ResponseDTOBuilder(ChatRoom chatRoom) {
            this.chatRoom = chatRoom;
        }
        
        public ResponseDTOBuilder withMetaInfo(ChatRoomMetaInfo metaInfo) {
            this.metaInfo = metaInfo;
            return this;
        }
        
        /**
         * 사용자 정보 설정
         */
        public ResponseDTOBuilder withUsers(ChatRoomUsers users) {
            this.users = users;
            return this;
        }
        
        /**
         * 마지막 메시지 정보 설정
         */
        public ResponseDTOBuilder withLastMessages(ChatRoomMessages lastMessages) {
            this.lastMessages = lastMessages;
            return this;
        }
        
        /**
         * 읽지 않은 메시지 수 설정
         */
        public ResponseDTOBuilder withUnreadCounts(UnreadMessageCounts unreadCounts) {
            this.unreadCounts = unreadCounts;
            return this;
        }
        
        /**
         * 상품 이미지 정보 설정
         */
        public ResponseDTOBuilder withArticleImages(ArticleImages articleImages) {
            this.articleImages = articleImages;
            return this;
        }
        
        /**
         * 지정된 정보를 기반으로 ChatRoomResponseDTO 객체를 생성합니다.
         */
        public ChatRoomResponseDTO build() {
            if (chatRoom == null || metaInfo == null) {
                return null;
            }
            
            Long chatRoomId = chatRoom.getId();
            var buyer = chatRoom.getBuyer();
            var seller = chatRoom.getSeller();
            
            if (buyer == null && seller == null) {
                return null;
            }
            
            Long buyerId = buyer != null ? buyer.getUserId() : null;
            Long sellerId = seller != null ? seller.getUserId() : null;
            
            // 사용자 정보 처리
            String buyerNickname;
            String sellerNickname;
            String buyerProfileImg;
            String sellerProfileImg;
            
            if (users != null) {
                buyerNickname = buyerId != null ? users.getUserNickname(buyerId, UNKNOWN_USER) : UNKNOWN_USER;
                sellerNickname = sellerId != null ? users.getUserNickname(sellerId, UNKNOWN_USER) : UNKNOWN_USER;
                buyerProfileImg = buyerId != null ? users.getUserProfileImage(buyerId) : null;
                sellerProfileImg = sellerId != null ? users.getUserProfileImage(sellerId) : null;
            } else {
                // 기본값 사용
                buyerNickname = buyer != null ? buyer.getNickname() : UNKNOWN_USER;
                sellerNickname = seller != null ? seller.getNickname() : UNKNOWN_USER;
                buyerProfileImg = buyer != null ? buyer.getProfileImg() : null;
                sellerProfileImg = seller != null ? seller.getProfileImg() : null;
            }
            
            // 마지막 메시지 처리
            ChatMessageResponseDTO lastMessageDTO = createLastMessageDTO(chatRoomId);
            
            // 아티클 이미지 URL 처리
            String articleImageUrl = getArticleImageUrl(chatRoom.getArticleId());
            
            // 읽지 않은 메시지 수 처리
            Integer unreadCount = getUnreadCount(chatRoomId);
            
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
        private ChatMessageResponseDTO createLastMessageDTO(Long chatRoomId) {
            Optional<ChatMessage> messageOpt = 
                lastMessages != null 
                    ? lastMessages.getLastMessage(chatRoomId) 
                    : Optional.ofNullable(metaInfo.getLastMessage());
            
            if (messageOpt.isEmpty()) {
                return null;
            }
            
            ChatMessage lastMessage = messageOpt.get();
            
            // 메시지 발신자 정보 확인
            String senderName = UNKNOWN_USER;
            Long senderId = lastMessage.getSenderId();
            
            if (users != null) {
                senderName = users.getUserNickname(senderId, UNKNOWN_USER);
            }
            
            return ChatMessageResponseDTO.builder()
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
        
        /**
         * 상품 이미지 URL을 가져옵니다.
         */
        private String getArticleImageUrl(Long articleId) {
            if (articleId == null) {
                return null;
            }
            
            return articleImages != null ? articleImages.getImageUrl(articleId) : null;
        }
        
        /**
         * 읽지 않은 메시지 수를 가져옵니다.
         */
        private Integer getUnreadCount(Long chatRoomId) {
            return unreadCounts != null ? unreadCounts.getUnreadCount(chatRoomId) : 0;
        }
    }
}