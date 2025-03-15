package com.ani.taku_backend.chatroom.service.query;

import com.ani.taku_backend.chatroom.domain.constant.ChatRoomStatus;
import com.ani.taku_backend.chatroom.domain.constant.JangterChatRole;
import com.ani.taku_backend.chatroom.domain.document.ChatMessage;
import com.ani.taku_backend.chatroom.domain.document.ChatRoomMetaInfo;
import com.ani.taku_backend.chatroom.domain.dto.response.ChatRoomResponseDTO;
import com.ani.taku_backend.chatroom.domain.entity.ChatRoom;
import com.ani.taku_backend.chatroom.domain.mapper.ChatRoomMapper;
import com.ani.taku_backend.chatroom.domain.repository.ChatRoomMetaRepository;
import com.ani.taku_backend.chatroom.domain.repository.ChatRoomRepository;
import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.exception.ErrorCode;
import com.ani.taku_backend.jangter.service.ProductImageService;
import com.ani.taku_backend.user.model.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
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
    private final ChatRoomMapper chatRoomMapper;
    private final ChatMessageQueryService chatMessageQueryService;


    private Map<String, Object> getChatRoomData(List<Long> chatRoomIds, Long userId) {
        Map<String, Object> result = new HashMap<>();

        if (chatRoomIds == null || chatRoomIds.isEmpty()) {
            result.put("metaInfos", List.of());
            result.put("lastMessageMap", Map.of());
            result.put("unreadCountMap", Map.of());
            return result;
        }


        List<ChatRoomMetaInfo> metaInfos = chatRoomMetaRepository.findMetaInfoWithLastMessages(chatRoomIds);

        Map<Long, ChatMessage> lastMessageMap = getLastMessagesFromMetaInfos(metaInfos);
        Map<Long, Integer> unreadCountMap = findUnreadCountMapFromMetaInfos(metaInfos, userId, chatRoomIds);

        result.put("metaInfos", metaInfos);
        result.put("lastMessageMap", lastMessageMap);
        result.put("unreadCountMap", unreadCountMap);

        return result;
    }

    private Map<Long, ChatMessage> getLastMessagesFromMetaInfos(List<ChatRoomMetaInfo> metaInfos) {
        Map<Long, ChatMessage> lastMessageMap = new HashMap<>();

        for (ChatRoomMetaInfo metaInfo : metaInfos) {
            Long chatRoomId = metaInfo.getChatRoomId();

            List<ChatMessage> messages = metaInfo.getMessages();
            if (messages != null && !messages.isEmpty()) {
                lastMessageMap.put(chatRoomId, messages.get(messages.size() - 1));
            }
        }

        return lastMessageMap;
    }

    private Map<Long, Integer> findUnreadCountMapFromMetaInfos(
            List<ChatRoomMetaInfo> metaInfos, Long userId, List<Long> chatRoomIds) {

        Map<Long, Integer> unreadCountMap = new HashMap<>();

        for (ChatRoomMetaInfo metaInfo : metaInfos) {
            Long chatRoomId = metaInfo.getChatRoomId();
            int unreadCount = metaInfo.getUnreadCount(userId);
            unreadCountMap.put(chatRoomId, unreadCount);
        }

        for (Long chatRoomId : chatRoomIds) {
            unreadCountMap.putIfAbsent(chatRoomId, 0);
        }

        return unreadCountMap;
    }


    public List<ChatRoomResponseDTO> findChatRoomList(Long userId) {
        List<ChatRoom> chatRooms = chatRoomRepository.findChatRoomsWithParticipantsAndUsers(userId, ChatRoomStatus.ACTIVE);

        if (chatRooms.isEmpty()) {
            return List.of();
        }

        return buildChatRoomResponseDTOs(chatRooms, userId);
    }

    public ChatRoomResponseDTO findChatRoom(String roomId, Long userId) {
        ChatRoom chatRoom = chatRoomRepository.findByWsRoomIdWithParticipantsAndUsers(roomId)
                .orElseThrow(() -> new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        ChatRoomMetaInfo chatRoomMetaInfo = chatRoomMetaRepository.findByChatRoomId(chatRoom.getId())
                .orElseThrow(() -> new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND));


        if (!chatRoomMetaInfo.getParticipants().containsUser(userId)) {
            throw new DuckwhoException(ErrorCode.INVALID_CHAT_USER);
        }


        Map<Long, User> userMap = buildUserMap(chatRoom);


        Map<Long, ChatMessage> lastMessageMap = chatMessageQueryService.findLastMessageMap(List.of(chatRoom.getId()));

        Map<Long, Integer> unreadCountMap = new HashMap<>();
        int unreadCount = chatRoomMetaInfo.getUnreadCount(userId);
        unreadCountMap.put(chatRoom.getId(), unreadCount);

        Map<Long, String> articleImageMap = getArticleImageMap(chatRoom.getArticleId());

        return chatRoomMapper.toChatRoomResponseDTO(
                chatRoom,
                chatRoomMetaInfo,
                userMap,
                lastMessageMap,
                unreadCountMap,
                articleImageMap
        );
    }


    private Map<Long, String> getArticleImageMap(Long articleId) {
        Map<Long, String> articleImageMap = new HashMap<>();
        if (articleId != null) {
            String imageUrl = productImageService.getProductImageUrl(articleId);
            articleImageMap.put(articleId, imageUrl);
        }
        return articleImageMap;
    }


    private Map<Long, User> buildUserMap(ChatRoom chatRoom) {
        Map<Long, User> userMap = new HashMap<>();
        User buyer = chatRoom.getBuyer();
        User seller = chatRoom.getSeller();

        if (buyer == null || seller == null) {
            throw new DuckwhoException(ErrorCode.INVALID_CHAT_USER);
        }

        userMap.put(buyer.getUserId(), buyer);
        userMap.put(seller.getUserId(), seller);
        return userMap;
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

        return buildChatRoomResponseDTOs(chatRooms, userId);
    }

    private List<ChatRoomResponseDTO> buildChatRoomResponseDTOs(List<ChatRoom> chatRooms, Long userId) {
        List<Long> chatRoomIds = extractChatRoomIds(chatRooms);

        Map<String, Object> chatRoomData = getChatRoomData(chatRoomIds, userId);

        List<ChatRoomMetaInfo> metaInfos = (List<ChatRoomMetaInfo>) chatRoomData.get("metaInfos");
        Map<Long, ChatRoomMetaInfo> metaInfoMap = createMetaInfoMap(metaInfos);

        Map<Long, User> userMap = createUserMap(chatRooms);

        List<Long> articleIds = extractArticleIds(chatRooms);
        Map<Long, String> articleImageMap = productImageService.getProductImageMap(articleIds);

        Map<Long, ChatMessage> lastMessageMap = (Map<Long, ChatMessage>) chatRoomData.get("lastMessageMap");

        Map<Long, Integer> unreadCountMap = (Map<Long, Integer>) chatRoomData.get("unreadCountMap");

        return chatRooms.stream()
                .map(room -> createChatRoomResponseDTO(
                        room,
                        metaInfoMap,
                        userMap,
                        lastMessageMap,
                        unreadCountMap,
                        articleImageMap))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 채팅방 ID 목록을 추출합니다.
     */
    private List<Long> extractChatRoomIds(List<ChatRoom> chatRooms) {
        return chatRooms.stream()
                .map(ChatRoom::getId)
                .collect(Collectors.toList());
    }

    private List<Long> extractArticleIds(List<ChatRoom> chatRooms) {
        return chatRooms.stream()
                .map(ChatRoom::getArticleId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    private Map<Long, ChatRoomMetaInfo> createMetaInfoMap(List<ChatRoomMetaInfo> metaInfos) {
        return metaInfos.stream()
                .collect(Collectors.toMap(
                        ChatRoomMetaInfo::getChatRoomId,
                        Function.identity(),
                        (existing, replacement) -> existing
                ));
    }

    private Map<Long, User> createUserMap(List<ChatRoom> chatRooms) {
        Map<Long, User> userMap = new HashMap<>();
        for (ChatRoom chatRoom : chatRooms) {
            User buyer = chatRoom.getBuyer();
            User seller = chatRoom.getSeller();

            if (buyer != null) userMap.put(buyer.getUserId(), buyer);
            if (seller != null) userMap.put(seller.getUserId(), seller);
        }
        return userMap;
    }


    private ChatRoomResponseDTO createChatRoomResponseDTO(
            ChatRoom room,
            Map<Long, ChatRoomMetaInfo> metaInfoMap,
            Map<Long, User> userMap,
            Map<Long, ChatMessage> lastMessageMap,
            Map<Long, Integer> unreadCountMap,
            Map<Long, String> articleImageMap) {

        ChatRoomMetaInfo metaInfo = metaInfoMap.get(room.getId());
        if (metaInfo == null || metaInfo.getParticipants() == null) {
            return null;
        }

        return chatRoomMapper.toChatRoomResponseDTO(
                room,
                metaInfo,
                userMap,
                lastMessageMap,
                unreadCountMap,
                articleImageMap
        );
    }
}