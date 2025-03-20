package com.ani.taku_backend.chatroom.service.query;

import com.ani.taku_backend.chatroom.domain.constant.ChatRoomStatus;
import com.ani.taku_backend.chatroom.domain.constant.JangterChatRole;
import com.ani.taku_backend.chatroom.domain.document.ChatMessage;
import com.ani.taku_backend.chatroom.domain.document.ChatRoomMetaInfo;
import com.ani.taku_backend.chatroom.domain.vo.ChatRoomMetaInfoData;
import com.ani.taku_backend.chatroom.domain.vo.ChatRoomMetaInfos;
import com.ani.taku_backend.chatroom.domain.dto.response.ChatRoomResponseDTO;
import com.ani.taku_backend.chatroom.domain.entity.ChatRoom;
import com.ani.taku_backend.chatroom.domain.repository.ChatRoomMetaRepository;
import com.ani.taku_backend.chatroom.domain.repository.ChatRoomRepository;
import com.ani.taku_backend.chatroom.domain.vo.ArticleImages;
import com.ani.taku_backend.chatroom.domain.vo.ChatRoomMessages;
import com.ani.taku_backend.chatroom.domain.vo.ChatRoomUsers;
import com.ani.taku_backend.chatroom.domain.vo.UnreadMessageCounts;
import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.exception.ErrorCode;
import com.ani.taku_backend.jangter.service.ProductImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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
    private final ProductImageService productImageService;


    public List<ChatRoomResponseDTO> findChatRoomList(Long userId) {
        List<ChatRoom> chatRooms = chatRoomRepository.findChatRoomsWithParticipantsAndUsers(userId, ChatRoomStatus.ACTIVE);

        if (chatRooms.isEmpty()) {
            return List.of();
        }

        List<Long> chatRoomIds = chatRooms.stream()
                .map(ChatRoom::getId)
                .collect(Collectors.toList());

        ChatRoomMetaInfoData metaInfoData = getChatRoomMetaInfoData(chatRoomIds, userId);
        ChatRoomMetaInfos metaInfos = metaInfoData.getMetaInfos();

        List<Long> articleIds = chatRooms.stream()
                .map(ChatRoom::getArticleId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        ArticleImages articleImages = productImageService.getArticleImages(articleIds);

        ChatRoomMessages lastMessages = metaInfoData.getLastMessages();
        UnreadMessageCounts unreadCounts = metaInfoData.getUnreadCounts();

        ChatRoomUsers users = ChatRoomUsers.fromChatRooms(chatRooms);

        return chatRooms.stream()
                .map(room -> {
                    Optional<ChatRoomMetaInfo> metaInfoOpt = metaInfos.getMetaInfo(room.getId());
                    if (metaInfoOpt.isEmpty()) {
                        return null;
                    }
                    return ChatRoomResponseDTO.from(
                            room, 
                            metaInfoOpt.get(), 
                            users, 
                            lastMessages, 
                            unreadCounts, 
                            articleImages
                    );
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public ChatRoomResponseDTO findChatRoom(String roomId, Long userId) {

        ChatRoom chatRoom = chatRoomRepository.findByWsRoomIdWithParticipantsAndUsers(roomId)
                .orElseThrow(() -> new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        ChatRoomMetaInfo chatRoomMetaInfo = chatRoomMetaRepository.findByChatRoomId(chatRoom.getId())
                .orElseThrow(() -> new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        chatRoomMetaInfo.validateUserAccess(userId);

        ChatRoomMessages lastMessages = ChatRoomMessages.of(
                chatRoom.getId(), 
                chatRoomMetaInfo.getLastMessage()
        );

        UnreadMessageCounts unreadCounts = UnreadMessageCounts.of(
                chatRoom.getId(), 
                chatRoomMetaInfo.getUnreadCount(userId)
        );

        ArticleImages articleImages = productImageService.getArticleImage(chatRoom.getArticleId());

        ChatRoomUsers users = ChatRoomUsers.fromChatRoom(chatRoom);

        return ChatRoomResponseDTO.from(
                chatRoom, 
                chatRoomMetaInfo, 
                users,
                lastMessages, 
                unreadCounts, 
                articleImages
        );
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

        List<Long> chatRoomIds = chatRooms.stream()
                .map(ChatRoom::getId)
                .collect(Collectors.toList());

        ChatRoomMetaInfoData metaInfoData = getChatRoomMetaInfoData(chatRoomIds, userId);
        ChatRoomMetaInfos metaInfos = metaInfoData.getMetaInfos();

        List<Long> articleIds = chatRooms.stream()
                .map(ChatRoom::getArticleId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        ArticleImages articleImages = productImageService.getArticleImages(articleIds);
        
        ChatRoomMessages lastMessages = metaInfoData.getLastMessages();
        UnreadMessageCounts unreadCounts = metaInfoData.getUnreadCounts();
        ChatRoomUsers users = ChatRoomUsers.fromChatRooms(chatRooms);
        
        return chatRooms.stream()
                .map(room -> {
                    Optional<ChatRoomMetaInfo> metaInfoOpt = metaInfos.getMetaInfo(room.getId());
                    if (metaInfoOpt.isEmpty()) {
                        return null;
                    }
                    return ChatRoomResponseDTO.from(
                            room, 
                            metaInfoOpt.get(), 
                            users, 
                            lastMessages, 
                            unreadCounts, 
                            articleImages
                    );
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private ChatRoomMetaInfoData getChatRoomMetaInfoData(List<Long> chatRoomIds, Long userId) {
        if (chatRoomIds == null || chatRoomIds.isEmpty()) {
            return ChatRoomMetaInfoData.empty();
        }

        List<ChatRoomMetaInfo> metaInfos = chatRoomMetaRepository.findMetaInfoWithLastMessages(chatRoomIds);

        ChatRoomMessages lastMessages = getLastMessagesFromMetaInfos(metaInfos);
        UnreadMessageCounts unreadCounts = findUnreadCountsFromMetaInfos(metaInfos, userId, chatRoomIds);

        return new ChatRoomMetaInfoData(metaInfos, lastMessages, unreadCounts);
    }


    private ChatRoomMessages getLastMessagesFromMetaInfos(List<ChatRoomMetaInfo> metaInfos) {
        Map<Long, ChatMessage> lastMessageMap = new HashMap<>();

        for (ChatRoomMetaInfo metaInfo : metaInfos) {
            Long chatRoomId = metaInfo.getChatRoomId();

            List<ChatMessage> messages = metaInfo.getMessages();
            if (messages != null && !messages.isEmpty()) {
                lastMessageMap.put(chatRoomId, messages.get(messages.size() - 1));
            }
        }

        return ChatRoomMessages.of(lastMessageMap);
    }

    private UnreadMessageCounts findUnreadCountsFromMetaInfos(
            List<ChatRoomMetaInfo> metaInfos, Long userId, List<Long> chatRoomIds) {

        Map<Long, Integer> unreadCountMap = new HashMap<>();

        if (userId == null) {
            // userId가 null이면 모든 채팅방의 읽지 않은 메시지 수를 0으로 설정
            chatRoomIds.forEach(id -> unreadCountMap.put(id, 0));
            return UnreadMessageCounts.of(unreadCountMap);
        }

        for (ChatRoomMetaInfo metaInfo : metaInfos) {
            Long chatRoomId = metaInfo.getChatRoomId();
            Integer unreadCount = metaInfo.getUnreadCount(userId);
            unreadCountMap.put(chatRoomId, unreadCount);
        }

        return UnreadMessageCounts.of(unreadCountMap);
    }
}