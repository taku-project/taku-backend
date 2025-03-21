package com.ani.taku_backend.chatroom.domain.dto.response;

import com.ani.taku_backend.chatroom.domain.document.ChatMessage;
import com.ani.taku_backend.chatroom.domain.document.ChatRoomMetaInfo;
import com.ani.taku_backend.chatroom.domain.entity.ChatRoom;
import com.ani.taku_backend.chatroom.domain.vo.ArticleImage;
import com.ani.taku_backend.chatroom.domain.vo.ChatRoomMessages;
import com.ani.taku_backend.chatroom.domain.vo.ChatRoomUsers;
import com.ani.taku_backend.chatroom.domain.vo.UnreadMessageCounts;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 채팅방 정보를 응답하기 위한 DTO 클래스
 */
@Getter
@ToString
public class ChatRoomResponseDTO {

    private static final Logger log = LoggerFactory.getLogger(ChatRoomResponseDTO.class);

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
            ArticleImage articleImage) {
        
        if (chatRoom == null || metaInfo == null || !chatRoom.isValid()) {
            return null;
        }
        
        return new ResponseDTOBuilder(chatRoom)
                .withMetaInfo(metaInfo)
                .withUsers(users)
                .withLastMessages(lastMessages)
                .withUnreadCounts(unreadCounts)
                .withArticleImages(articleImage)
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
        private ArticleImage articleImage;
        
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
        public ResponseDTOBuilder withArticleImages(ArticleImage articleImage) {
            this.articleImage = articleImage;
            return this;
        }

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
                buyerNickname = buyerId != null ? users.getUserNicknameOrUnknown(buyerId) : UNKNOWN_USER;
                sellerNickname = sellerId != null ? users.getUserNicknameOrUnknown(sellerId) : UNKNOWN_USER;
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
        

        private ChatMessageResponseDTO createLastMessageDTO(Long chatRoomId) {
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
                : UNKNOWN_USER;
            
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
        private String getArticleImageUrl(Long articleId) {
            if (articleId == null || articleImage == null) {
                return null;
            }
            try {
                return articleImage.getImageUrl(articleId);
            } catch (Exception e) {
                // NPE나 다른 예외 발생 시 로깅하고 null 반환
                log.warn("상품 이미지 조회 중 오류 발생: articleId={}, error={}", articleId, e.getMessage());
                return null;
            }
        }
        
        /**
         * 읽지 않은 메시지 수를 가져옵니다.
         */
        private Integer getUnreadCount(Long chatRoomId) {
            return unreadCounts != null ? unreadCounts.getUnreadCount(chatRoomId) : 0;
        }
    }
}