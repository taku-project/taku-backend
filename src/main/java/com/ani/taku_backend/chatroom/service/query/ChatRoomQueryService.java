package com.ani.taku_backend.chatroom.service.query;

import com.ani.taku_backend.chatroom.domain.constant.ChatRoomStatus;
import com.ani.taku_backend.chatroom.domain.constant.JangterChatRole;
import com.ani.taku_backend.chatroom.domain.document.ChatRoomMetaInfo;
import com.ani.taku_backend.chatroom.domain.vo.ChatRoomMetaInfoData;
import com.ani.taku_backend.chatroom.domain.vo.ChatRoomMetaInfos;
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
        
        return new ChatRoomCompositeDTO(metaInfos, articleImage, lastMessages, unreadCounts, users, chatRoomDtoConverter);
    }


    public ChatRoomResponseDTO findChatRoom(String roomId, Long userId) {

        ChatRoom chatRoom = validateChatRoomAccess(roomId, userId);

        ChatRoomMetaInfo metaInfo = chatRoomMetaRepository.findByChatRoomId(chatRoom.getId())
                .orElseThrow(() -> new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND));
        
        ChatRoomMessages lastMessages = ChatRoomMessages.of(
                chatRoom.getId(), 
                metaInfo.getLastMessage()
        );

        UnreadMessageCounts unreadCounts = UnreadMessageCounts.of(
                chatRoom.getId(), 
                metaInfo.getUnreadCount(userId)
        );

        List<ProductImageDTO> productImages = duckuJangterRepository.findProductImagesById(List.of(chatRoom.getArticleId()));
        ArticleImage articleImage = ArticleImage.fromProductImageDTOs(productImages);
        
        ChatRoomUsers users = ChatRoomUsers.fromChatRoom(chatRoom);

        return chatRoomDtoConverter.toResponseDto(
                chatRoom, 
                metaInfo, 
                users,
                lastMessages, 
                unreadCounts,
                articleImage
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