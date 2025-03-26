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
import java.util.stream.Collectors;

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


    /**
     * 특정 채팅방의 정보를 조회합니다.
     *
     * @param roomId 채팅방 ID
     * @param userId 사용자 ID
     * @return 채팅방 정보
     */
    public ChatRoomResponseDTO findChatRoom(String roomId, Long userId) {
        // 1. 채팅방 조회 및 검증
        ChatRoom chatRoom = findChatRoomByWsRoomId(roomId);

        chatRoom.validateStatus();
        chatRoom.validateUserAccess(userId);

        // 2. 메타 정보 조회
        ChatRoomMetaInfo metaInfo = chatRoomMetaRepository.findByChatRoomId(chatRoom.getId())
                .orElseThrow(() -> new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        // 3. 필요한 데이터 준비
        ChatRoomUsers users = ChatRoomUsers.fromChatRoom(chatRoom);
        ChatRoomMessages lastMessages = ChatRoomMessages.of(chatRoom.getId(), metaInfo.getLastMessage());
        UnreadMessageCounts unreadCounts = UnreadMessageCounts.of(chatRoom.getId(), metaInfo.getUnreadCount(userId));
        ArticleImage articleImage = findArticleImage(List.of(chatRoom.getArticleId()));

        // 4. DTO 변환 및 반환
        return createChatRoomResponseDTO(
                chatRoom,
                metaInfo,
                users,
                lastMessages,
                unreadCounts,
                articleImage
        );
    }


    /**
     * 사용자의 채팅방 목록을 페이징하여 조회합니다(무한 스크롤).
     * 
     * @param userId 사용자 ID
     * @param pageable 페이징 정보
     * @return 채팅방 응답 DTO 목록의 Slice
     */
    public Slice<ChatRoomResponseDTO> findChatRoomListWithSlice(Long userId, Pageable pageable) {
        // 채팅방과 참여자, 사용자를 함께 조회하도록 쿼리 최적화
        Slice<ChatRoom> chatRooms = chatRoomRepository.findChatRoomsWithSlice(
                userId, pageable, ChatRoomStatus.ACTIVE);

        if (!chatRooms.hasContent()) {
            return new SliceImpl<>(Collections.emptyList(), pageable, false);
        }
        
        List<Long> chatRoomIds = ChatRoom.extractChatRoomIds(chatRooms.getContent());
        List<Long> articleIds = ChatRoom.extractArticleIds(chatRooms.getContent());

        // 필요한 모든 데이터를 한 번에 배치로 조회
        ChatRoomCompositeDTO dataBundle = aggregateChatRoomData(
                chatRooms.getContent(), chatRoomIds, articleIds, userId);
        
        List<ChatRoomResponseDTO> responseDTOs = dataBundle.toChatRoomResponseDTOs(chatRooms.getContent());
        
        return new SliceImpl<>(responseDTOs, pageable, chatRooms.hasNext());
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

        // 1. 마지막 메시지 조회
        return getLastMessage(chatRoomId, lastMessages, metaInfo)
                .filter(msg -> msg.getSenderId() != null)
                .map(lastMessage -> createResponseDTO(
                        lastMessage, 
                        getUserName(users, lastMessage.getSenderId()), 
                        chatRoom.getWsRoomId(), 
                        chatRoomId))
                .orElse(null);
    }


    /**
     * 채팅방의 마지막 메시지를 조회합니다.
     */
    private Optional<ChatMessage> getLastMessage(
            Long chatRoomId, 
            ChatRoomMessages lastMessages, 
            ChatRoomMetaInfo metaInfo) {
        
        try {
            return lastMessages.getLastMessage(chatRoomId)
                    .or(() -> Optional.ofNullable(metaInfo.getLastMessage()));
        } catch (Exception e) {
            log.warn("마지막 메시지 조회 중 오류 발생: chatRoomId={}, error={}", chatRoomId, e.getMessage());
            return Optional.empty();
        }
    }
    
    /**
     * 발신자 이름을 결정합니다.
     */
    private String getUserName(ChatRoomUsers users, Long senderId) {
        return users.getUserNicknameOrUnknown(senderId);
    }
    
    /**
     * 채팅 메시지 응답 DTO를 생성합니다.
     */
    private ChatMessageResponseDTO createResponseDTO(
            ChatMessage message, 
            String senderName, 
            String wsRoomId,
            Long chatRoomId) {
        
        try {
            return ChatMessageResponseDTO.from(message, senderName, wsRoomId);
        } catch (Exception e) {
            log.warn("메시지 DTO 생성 중 오류 발생: chatRoomId={}, error={}", chatRoomId, e.getMessage());
            return null;
        }
    }
    
    /**
     * 상품 이미지 URL 조회
     */
    private String getArticleImageUrl(Long articleId, ArticleImage articleImage) {
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
        return unreadCounts.getUnreadCount(chatRoomId);
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

        // 1. 채팅방 조회
        ChatRoom chatRoom = findChatRoomByWsRoomId(wsRoomId);
        
        // 2. 채팅방 상태 및 사용자 권한 검증
        chatRoom.validateStatus();
        chatRoom.validateUserAccess(userId);
        
        // 3. 메타 정보 검증
        validateMetaInfoAccess(chatRoom.getId(), userId);
        
        return chatRoom;
    }
    
    /**
     * WebSocket 룸 ID로 채팅방을 조회합니다.
     * 
     * @param wsRoomId WebSocket 룸 ID
     * @return 조회된 채팅방
     * @throws DuckwhoException 채팅방을 찾을 수 없는 경우
     */
    private ChatRoom findChatRoomByWsRoomId(String wsRoomId) {
        return chatRoomRepository.findByWsRoomIdWithParticipantsAndUsers(wsRoomId)
                .orElseThrow(() -> new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND));
    }

    
    /**
     * 채팅방 메타 정보의 접근 권한을 검증합니다.
     * 
     * @param chatRoomId 채팅방 ID
     * @param userId 사용자 ID
     * @throws DuckwhoException 메타 정보를 찾을 수 없거나 접근 권한이 없는 경우
     */
    private void validateMetaInfoAccess(Long chatRoomId, Long userId) {
        ChatRoomMetaInfo metaInfo = chatRoomMetaRepository.findByChatRoomId(chatRoomId)
                .orElseThrow(() -> new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND));
                
        metaInfo.validateUserAccess(userId);
    }


    public Integer getTotalUnreadCount(Long userId) {
        List<ChatRoomMetaInfo> userChatrooms = chatRoomMetaRepository
                .findChatRoomMetaInfosByParticipantUserId(userId);

        if (userChatrooms.isEmpty()) {
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
        List<Long> articleIds = ChatRoom.extractArticleIds(chatRooms);

        ChatRoomCompositeDTO dataBundle = aggregateChatRoomData(
                chatRooms, chatRoomIds, articleIds, userId);
        
        return dataBundle.toChatRoomResponseDTOs(chatRooms);
    }


    private ChatRoomMetaInfoData getChatRoomMetaInfoData(List<Long> chatRoomIds, Long userId) {
        if (chatRoomIds.isEmpty()) {
            return ChatRoomMetaInfoData.empty();
        }

        List<ChatRoomMetaInfo> metaInfos = chatRoomMetaRepository.findMetaInfoWithLastMessages(chatRoomIds);
        
        ChatRoomMessages lastMessages = ChatRoomMessages.fromMetaInfos(metaInfos);
        UnreadMessageCounts unreadCounts = UnreadMessageCounts.fromMetaInfos(metaInfos, userId, chatRoomIds);

        return new ChatRoomMetaInfoData(metaInfos, lastMessages, unreadCounts);
    }

    private ChatRoomCompositeDTO aggregateChatRoomData(
            List<ChatRoom> chatRooms, 
            List<Long> chatRoomIds, 
            List<Long> articleIds, 
            Long userId) {

        ChatRoomMetaInfoData metaInfoData = getChatRoomMetaInfoData(chatRoomIds, userId);
        ChatRoomMetaInfos metaInfos = metaInfoData.getMetaInfos();

        ArticleImage articleImage = findArticleImage(articleIds);
        
        ChatRoomMessages lastMessages = metaInfoData.getLastMessages();
        UnreadMessageCounts unreadCounts = metaInfoData.getUnreadCounts();
        
        // 사용자 정보 준비
        ChatRoomUsers users = ChatRoomUsers.fromChatRooms(chatRooms);
        
        return new ChatRoomCompositeDTO(metaInfos, articleImage, lastMessages, unreadCounts, users, this);
    }
    
    /**
     * 상품 이미지를 조회합니다.
     */
    private ArticleImage findArticleImage(List<Long> articleIds) {
        if (articleIds.isEmpty()) {
            return ArticleImage.empty();
        }
        
        // 중복 제거 및 null 필터링
        List<Long> distinctArticleIds = articleIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        
        if (distinctArticleIds.isEmpty()) {
            return ArticleImage.empty();
        }
        
        // 모든 상품 이미지 조회
        List<ProductImageDTO> productImages = duckuJangterRepository.findProductImagesById(distinctArticleIds);
        return ArticleImage.fromProductImageDTOs(productImages);
    }

    /**
     * 사용자의 모든 채팅방 목록을 조회합니다.
     * 
     * @param userId 사용자 ID
     * @return 채팅방 응답 DTO 목록
     */
    public List<ChatRoomResponseDTO> findChatRoomList(Long userId) {
        Slice<ChatRoomResponseDTO> slice = findChatRoomListWithSlice(userId, Pageable.unpaged());
        return slice.getContent();
    }

}