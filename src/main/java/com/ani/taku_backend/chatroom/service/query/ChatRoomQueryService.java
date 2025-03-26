package com.ani.taku_backend.chatroom.service.query;

import com.ani.taku_backend.chatroom.contansts.MessageConstants;
import com.ani.taku_backend.chatroom.domain.constant.ChatRoomStatus;
import com.ani.taku_backend.chatroom.domain.constant.JangterChatRole;
import com.ani.taku_backend.chatroom.domain.document.ChatMessage;
import com.ani.taku_backend.chatroom.domain.document.ChatRoomMetaInfo;
import com.ani.taku_backend.chatroom.domain.vo.ChatRoomMetaInfoData;
import com.ani.taku_backend.chatroom.domain.vo.ChatRoomMetaInfos;
import com.ani.taku_backend.chatroom.dto.response.ChatMessageResponseDTO;
import com.ani.taku_backend.chatroom.dto.response.ChatRoomCompositeDTO;
import com.ani.taku_backend.chatroom.dto.response.ChatRoomResponseDTO;
import com.ani.taku_backend.chatroom.domain.entity.ChatRoom;
import com.ani.taku_backend.chatroom.domain.repository.ChatRoomMetaRepository;
import com.ani.taku_backend.chatroom.domain.repository.ChatRoomRepository;
import com.ani.taku_backend.chatroom.domain.vo.ArticleImage;
import com.ani.taku_backend.chatroom.domain.vo.ChatRoomMessages;
import com.ani.taku_backend.chatroom.domain.vo.ChatRoomUsers;
import com.ani.taku_backend.chatroom.domain.vo.UnreadMessageCounts;
import com.ani.taku_backend.chatroom.mapper.ChatRoomDtoConverter;
import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.exception.ErrorCode;
import com.ani.taku_backend.jangter.model.dto.ProductImageDTO;
import com.ani.taku_backend.jangter.repository.DuckuJangterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.Optional;

/**
 * 채팅방 조회 서비스
 * Query 파트를 담당하는 서비스입니다.
 * 채팅방 조회와 관련된 비즈니스 로직만 담당합니다.
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChatRoomQueryService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMetaRepository chatRoomMetaRepository;
    private final DuckuJangterRepository duckuJangterRepository;
    private final ChatRoomDtoConverter chatRoomDtoConverter;


    public List<ChatRoomResponseDTO> findChatRoomList(Long userId) {
        List<ChatRoom> chatRooms = chatRoomRepository.findChatRoomsWithParticipantsAndUsers(userId, ChatRoomStatus.ACTIVE);

        if (chatRooms.isEmpty()) {
            return List.of();
        }
        
        List<Long> chatRoomIds = ChatRoom.extractChatRoomIds(chatRooms);

        ChatRoomCompositeDTO dataBundle = aggregateChatRoomData(chatRooms, chatRoomIds, userId);
        
        return dataBundle.toChatRoomResponseDTOs(chatRooms);
    }

    /**
     * 사용자의 채팅방 목록을 페이징하여 조회합니다(무한 스크롤).
     * 
     * @param userId 사용자 ID
     * @param pageable 페이징 정보
     * @return 채팅방 응답 DTO 목록의 Slice
     */
    public Slice<ChatRoomResponseDTO> findChatRoomListWithSlice(Long userId, Pageable pageable) {
        Slice<ChatRoom> chatRooms = chatRoomRepository.findChatRoomsWithSlice(
                userId, pageable, ChatRoomStatus.ACTIVE);

        if (!chatRooms.hasContent()) {
            return new SliceImpl<>(Collections.emptyList(), pageable, false);
        }
        
        List<Long> chatRoomIds = ChatRoom.extractChatRoomIds(chatRooms.getContent());

        ChatRoomCompositeDTO dataBundle = aggregateChatRoomData(chatRooms.getContent(), chatRoomIds, userId);
        
        List<ChatRoomResponseDTO> responseDTOs = dataBundle.toChatRoomResponseDTOs(chatRooms.getContent());
        
        return new SliceImpl<>(responseDTOs, pageable, chatRooms.hasNext());
    }


    private ChatRoomCompositeDTO aggregateChatRoomData(List<ChatRoom> chatRooms, List<Long> chatRoomIds, Long userId) {
        ChatRoomMetaInfoData metaInfoData = getChatRoomMetaInfoData(chatRoomIds, userId);
        ChatRoomMetaInfos metaInfos = metaInfoData.getMetaInfos();
        
        List<Long> articleIds = ChatRoom.extractArticleIds(chatRooms);
        List<ProductImageDTO> productImages = duckuJangterRepository.findProductImagesById(articleIds);
        ArticleImage articleImage = ArticleImage.fromProductImageDTOs(productImages);
        
        ChatRoomMessages lastMessages = metaInfoData.getLastMessages();
        UnreadMessageCounts unreadCounts = metaInfoData.getUnreadCounts();
        
        ChatRoomUsers users = ChatRoomUsers.fromChatRooms(chatRooms);
        
        return new ChatRoomCompositeDTO(metaInfos, articleImage, lastMessages, unreadCounts, users, this);
    }


    public ChatRoomResponseDTO findChatRoom(String roomId, Long userId) {
        // 1. 채팅방 조회 및 검증
        ChatRoom chatRoom = findAndValidateChatRoom(roomId, userId);
        
        // 2. 메타 정보 조회
        ChatRoomMetaInfo metaInfo = findChatRoomMetaInfo(chatRoom.getId());
        
        // 3. 필요한 데이터 준비
        ChatRoomUsers users = prepareUserInfo(chatRoom);
        ChatRoomMessages lastMessages = prepareLastMessages(chatRoom.getId(), metaInfo);
        UnreadMessageCounts unreadCounts = prepareUnreadCounts(chatRoom.getId(), userId, metaInfo);
        ArticleImage articleImage = prepareArticleImage(chatRoom.getArticleId());
        
        // 4. 추가 데이터 처리
        ChatMessageResponseDTO lastMessageDTO = createLastMessageDTO(chatRoom, chatRoom.getId(), lastMessages, metaInfo, users);
        String articleImageUrl = getArticleImageUrl(chatRoom.getArticleId(), articleImage);
        Integer unreadCount = getUnreadCount(chatRoom.getId(), unreadCounts);
        
        // 5. DTO 변환
        return chatRoomDtoConverter.toResponseDto(
                chatRoom, 
                metaInfo, 
                users,
                lastMessages, 
                unreadCounts,
                articleImage,
                lastMessageDTO,
                articleImageUrl,
                unreadCount
        );
    }
    
    /**
     * 채팅방 조회 및 기본 검증
     */
    private ChatRoom findAndValidateChatRoom(String roomId, Long userId) {
        ChatRoom chatRoom = chatRoomRepository.findByWsRoomIdWithParticipantsAndUsers(roomId)
                .orElseThrow(() -> new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND));
        
        chatRoom.validateStatus();
        chatRoom.validateUserAccess(userId);
        
        return chatRoom;
    }

    /**
     * 메타 정보 조회
     */
    private ChatRoomMetaInfo findChatRoomMetaInfo(Long chatRoomId) {
        return chatRoomMetaRepository.findByChatRoomId(chatRoomId)
                .orElseThrow(() -> new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND));
    }
    
    /**
     * 사용자 정보 준비
     */
    private ChatRoomUsers prepareUserInfo(ChatRoom chatRoom) {
        return ChatRoomUsers.fromChatRoom(chatRoom);
    }
    
    /**
     * 마지막 메시지 정보 준비
     */
    private ChatRoomMessages prepareLastMessages(Long chatRoomId, ChatRoomMetaInfo metaInfo) {
        return ChatRoomMessages.of(chatRoomId, metaInfo.getLastMessage());
    }
    
    /**
     * 읽지 않은 메시지 수 준비
     */
    private UnreadMessageCounts prepareUnreadCounts(Long chatRoomId, Long userId, ChatRoomMetaInfo metaInfo) {
        return UnreadMessageCounts.of(chatRoomId, metaInfo.getUnreadCount(userId));
    }
    
    /**
     * 상품 이미지 준비
     */
    private ArticleImage prepareArticleImage(Long articleId) {
        List<ProductImageDTO> productImages = duckuJangterRepository.findProductImagesById(List.of(articleId));
        return ArticleImage.fromProductImageDTOs(productImages);
    }
    
    /**
     * 마지막 메시지 DTO 생성
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
     * 상품 이미지 URL 조회
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
     * 읽지 않은 메시지 수 조회
     */
    private Integer getUnreadCount(Long chatRoomId, UnreadMessageCounts unreadCounts) {
        return unreadCounts != null ? unreadCounts.getUnreadCount(chatRoomId) : 0;
    }

    /**
     * 채팅방 응답 DTO를 생성합니다.
     * 각 계산값(lastMessageDTO, articleImageUrl, unreadCount)을 직접 계산하여 DTO 생성에 사용합니다.
     */
    public ChatRoomResponseDTO createChatRoomResponseDTO(
            ChatRoom chatRoom,
            ChatRoomMetaInfo metaInfo,
            ChatRoomUsers users,
            ChatRoomMessages lastMessages,
            UnreadMessageCounts unreadCounts,
            ArticleImage articleImage) {
        
        // 마지막 메시지 조회
        ChatMessageResponseDTO lastMessageDTO = createLastMessageDTO(
            chatRoom, chatRoom.getId(), lastMessages, metaInfo, users);
        
        // 이미지 URL 조회
        String articleImageUrl = getArticleImageUrl(chatRoom.getArticleId(), articleImage);
        
        // 읽지 않은 메시지 수 조회
        Integer unreadCount = getUnreadCount(chatRoom.getId(), unreadCounts);
        
        return chatRoomDtoConverter.toResponseDto(
                chatRoom, 
                metaInfo, 
                users,
                lastMessages, 
                unreadCounts,
                articleImage,
                lastMessageDTO,
                articleImageUrl,
                unreadCount
        );
    }

    public ChatRoom validateChatRoomAccess(String wsRoomId, Long userId) {
        log.debug("채팅방 접근 권한 검증: wsRoomId={}, userId={}", wsRoomId, userId);

        ChatRoom chatRoom = chatRoomRepository.findByWsRoomIdWithParticipantsAndUsers(wsRoomId)
                .orElseThrow(() -> new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        ChatRoomMetaInfo metaInfo = chatRoomMetaRepository.findByChatRoomId(chatRoom.getId())
                .orElseThrow(() -> new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        metaInfo.validateUserAccess(userId);
        
        return chatRoom;
    }


    public Integer getTotalUnreadCount(Long userId) {
        List<ChatRoomMetaInfo> userChatrooms = chatRoomMetaRepository
                .findChatRoomMetaInfosByParticipantUserId(userId);

        if (userChatrooms == null || userChatrooms.isEmpty()) {
            return 0;
        }

        return ChatRoomMetaInfo.calculateTotalUnreadCount(userChatrooms, userId);
    }


    public List<ChatRoomResponseDTO> findChatRoomListByRole(Long userId, JangterChatRole role) {
        List<ChatRoom> chatRooms = chatRoomRepository.findChatRoomsByUserIdAndRole(
                userId, role, ChatRoomStatus.ACTIVE);

        if (chatRooms.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> chatRoomIds = ChatRoom.extractChatRoomIds(chatRooms);

        ChatRoomCompositeDTO dataBundle = aggregateChatRoomData(chatRooms, chatRoomIds, userId);
        
        return dataBundle.toChatRoomResponseDTOs(chatRooms);
    }


    private ChatRoomMetaInfoData getChatRoomMetaInfoData(List<Long> chatRoomIds, Long userId) {
        if (chatRoomIds == null || chatRoomIds.isEmpty()) {
            return ChatRoomMetaInfoData.empty();
        }

        List<ChatRoomMetaInfo> metaInfos = chatRoomMetaRepository.findMetaInfoWithLastMessages(chatRoomIds);
        
        ChatRoomMessages lastMessages = ChatRoomMessages.fromMetaInfos(metaInfos);
        UnreadMessageCounts unreadCounts = UnreadMessageCounts.fromMetaInfos(metaInfos, userId, chatRoomIds);

        return new ChatRoomMetaInfoData(metaInfos, lastMessages, unreadCounts);
    }
}