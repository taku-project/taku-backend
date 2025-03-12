package com.ani.taku_backend.chatroom.service;

import com.ani.taku_backend.chatroom.domain.constant.ChatRoomStatus;
import com.ani.taku_backend.chatroom.domain.constant.ParticipantRole;
import com.ani.taku_backend.chatroom.domain.document.ChatMessage;
import com.ani.taku_backend.chatroom.domain.document.ChatRoomMetaInfo;
import com.ani.taku_backend.chatroom.domain.document.ParticipantInfo;
import com.ani.taku_backend.chatroom.domain.document.Participants;
import com.ani.taku_backend.chatroom.domain.dto.request.ChatRoomRequestDTO;
import com.ani.taku_backend.chatroom.domain.dto.response.ChatRoomResponseDTO;
import com.ani.taku_backend.chatroom.domain.entity.ChatRoom;
import com.ani.taku_backend.chatroom.domain.mapper.ChatRoomMapper;
import com.ani.taku_backend.chatroom.domain.repository.ChatMessageRepository;
import com.ani.taku_backend.chatroom.domain.repository.ChatRoomMetaRepository;
import com.ani.taku_backend.chatroom.domain.repository.ChatRoomRepository;
import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.exception.ErrorCode;
import com.ani.taku_backend.jangter.service.ProductImageService;
import com.ani.taku_backend.user.model.entity.User;
import com.ani.taku_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ani.taku_backend.jangter.model.entity.DuckuJangter;
import com.ani.taku_backend.jangter.model.enums.ProductStatus;
import com.ani.taku_backend.jangter.repository.DuckuJangterRepository;
import com.ani.taku_backend.chatroom.domain.dto.ChatRoomDetailDTO;


import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 채팅방 및 관련 메타데이터를 관리하는 서비스입니다.
 * 채팅방 생성, 조회, 참여자 관리 등의 기능을 제공합니다.
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMetaRepository chatRoomMetaRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final DuckuJangterRepository duckuJangterRepository;
    private final UserRepository userRepository;
    private final ProductImageService productImageService;
    private final ChatRoomMapper chatRoomMapper;
    private final ChatService chatService;

    public static final String UNKNOWN_USER = "알 수 없음";

    /**
     * 새로운 채팅방을 생성합니다.
     *
     * @param requestDto 채팅방 생성 요청 정보
     * @return 생성된 채팅방 정보
     */
    @Transactional
    public ChatRoomResponseDTO createChatRoom(ChatRoomRequestDTO requestDto) {

        DuckuJangter product = duckuJangterRepository.findById(requestDto.articleId())
                .orElseThrow(() -> new DuckwhoException(ErrorCode.NOT_FOUND_POST));

        Long sellerId = product.getUser().getUserId();

        // 구매자가 판매자와 동일인물이면 안 됨
        if (sellerId.equals(requestDto.buyerId())) {
            throw new DuckwhoException(ErrorCode.INVALID_CHAT_USER);
        }

        // 게시글 상태 확인 (판매중인 상태인지)
        if (product.getStatus() != ProductStatus.FOR_SALE) {
            throw new DuckwhoException(ErrorCode.INVALID_PRODUCT_STATUS);
        }

        validateNewChatRoom(requestDto);

        ChatRoom chatRoom = ChatRoom.builder()
                .articleId(requestDto.articleId())
                .build();

        ChatRoom savedRoom = chatRoomRepository.save(chatRoom);

        ChatRoomMetaInfo metaInfo = ChatRoomMetaInfo.builder()
                .chatRoomId(savedRoom.getId())
                .build();

        metaInfo.initializeParticipants(requestDto.buyerId(), sellerId);
        chatRoomMetaRepository.save(metaInfo);

        User buyer = userRepository.findById(requestDto.buyerId()).orElse(null);
        User seller = userRepository.findById(sellerId).orElse(null);

        String articleThumbnailUrl = productImageService.getProductImageUrl(requestDto.articleId());

        Map<Long, User> userMap = new HashMap<>();
        if (buyer != null) userMap.put(buyer.getUserId(), buyer);
        if (seller != null) userMap.put(seller.getUserId(), seller);
        
        Map<Long, ChatMessage> lastMessageMap = new HashMap<>();
        Map<Long, Integer> unreadCountMap = new HashMap<>();
        unreadCountMap.put(savedRoom.getId(), 0);
        
        Map<Long, String> articleImageMap = new HashMap<>();
        articleImageMap.put(requestDto.articleId(), articleThumbnailUrl);

        return chatRoomMapper.toChatRoomResponseDTO(
                savedRoom,
                metaInfo,
                userMap,
                lastMessageMap,
                unreadCountMap,
                articleImageMap
        );
    }

    /**
     * 사용자의 채팅방 목록을 조회합니다.
     * @param userId 사용자 ID
     * @return 채팅방 응답 DTO 목록
     */
    @Transactional(readOnly = true)
    public List<ChatRoomResponseDTO> findChatRoomListOptimized(Long userId) {

        List<ChatRoomDetailDTO> chatRooms = chatRoomRepository
                .findChatRoomsWithDetailsForUser(userId, ChatRoomStatus.ACTIVE);
        
        if (chatRooms.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> chatRoomIds = chatRooms.stream()
                .map(ChatRoomDetailDTO::getId)
                .collect(Collectors.toList());

        List<ChatRoomMetaInfo> metaInfos = chatRoomMetaRepository.findByChatRoomIdIn(chatRoomIds);
        Map<Long, ChatRoomMetaInfo> metaInfoMap = metaInfos.stream()
                .collect(Collectors.toMap(
                        ChatRoomMetaInfo::getChatRoomId,
                        metaInfo -> metaInfo,
                        (existing, replacement) -> existing
                ));

        List<Long> participantIds = metaInfos.stream()
                .filter(meta -> meta.getParticipants() != null)
                .flatMap(meta -> meta.getParticipants().getAllParticipantIds().stream())
                .distinct()
                .collect(Collectors.toList());

        List<User> users = userRepository.findByUserIdIn(participantIds);
        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(
                        User::getUserId,
                        user -> user,
                        (existing, replacement) -> existing
                ));

        Map<Long, ChatMessage> lastMessageMap = chatService.getLastMessageMap(chatRoomIds);

        Map<Long, Integer> unreadCountMap = createUnreadCountMap(chatRoomIds, userId);

        List<Long> articleIds = chatRooms.stream()
                .map(ChatRoomDetailDTO::getArticleId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        
        Map<Long, String> articleImageMap = productImageService.getProductImageMap(articleIds);
        
        // ChatRoom 객체를 조회하여 매퍼로 변환
        List<ChatRoom> roomEntities = chatRoomRepository.findAllById(chatRoomIds);
        Map<Long, ChatRoom> roomMap = roomEntities.stream()
                .collect(Collectors.toMap(
                        ChatRoom::getId,
                        room -> room,
                        (existing, replacement) -> existing
                ));
        
        return chatRooms.stream()
                .map(dto -> {
                    ChatRoomMetaInfo metaInfo = metaInfoMap.get(dto.getId());
                    ChatRoom room = roomMap.get(dto.getId());
                    if (metaInfo == null || metaInfo.getParticipants() == null || room == null) {
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
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 사용자의 채팅방 목록을 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 채팅방 응답 DTO 목록
     */
    @Transactional(readOnly = true)
    public List<ChatRoomResponseDTO> findChatRoomList(Long userId) {
        List<ChatRoomMetaInfo> connectedChatRoomMetaInfos = getConnectedChatRoomMetaInfos(userId);
        
        if (connectedChatRoomMetaInfos.isEmpty()) {
            return null;
        }

        List<Long> chatRoomIds = connectedChatRoomMetaInfos.stream()
                .map(ChatRoomMetaInfo::getChatRoomId)
                .collect(Collectors.toList());
        
        if (chatRoomIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<ChatRoom> userChatRooms = chatRoomRepository
                .findByIdInAndStatusOptimized(chatRoomIds, ChatRoomStatus.ACTIVE);

        Map<Long, ChatRoomMetaInfo> chatRoomMetaInfoMap = connectedChatRoomMetaInfos.stream()
                .collect(Collectors.toMap(
                        ChatRoomMetaInfo::getChatRoomId,
                        metaInfo -> metaInfo,
                        (existing, replacement) -> existing
                ));

        List<Long> participantIds = connectedChatRoomMetaInfos.stream()
                .flatMap(metaInfo -> metaInfo.getParticipants().getInfo().values().stream()
                        .map(ParticipantInfo::getUserId))
                .distinct()
                .collect(Collectors.toList());

        List<User> users = userRepository.findByUserIdIn(participantIds);
        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(
                        User::getUserId,
                        user -> user,
                        (existing, replacement) -> existing
                ));

        Map<Long, ChatMessage> lastMessageMap = chatService.getLastMessageMap(chatRoomIds);

        Map<Long, Integer> unreadCountMap = createUnreadCountMap(chatRoomIds, userId);

        List<Long> articleIds = userChatRooms.stream()
                .map(ChatRoom::getArticleId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, String> articleImageMap = productImageService.getProductImageMap(articleIds);

        return userChatRooms.stream()
                .map(chatRoom -> chatRoomMapper.toChatRoomResponseDTO(
                        chatRoom,
                        chatRoomMetaInfoMap.get(chatRoom.getId()),
                        userMap,
                        lastMessageMap,
                        unreadCountMap,
                        articleImageMap
                ))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 특정 채팅방의 정보를 조회합니다.
     *
     * @param roomId 채팅방 ID
     * @param userId 사용자 ID
     * @return 채팅방 정보
     */
    public ChatRoomResponseDTO findChatRoom(String roomId, Long userId) {
        ChatRoom chatRoom = chatRoomRepository.findByWsRoomId(roomId)
                .orElseThrow(() -> new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        ChatRoomMetaInfo chatRoomMetaInfo = chatRoomMetaRepository.findByChatRoomId(chatRoom.getId())
                .orElseThrow(() -> new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        Participants participants = chatRoomMetaInfo.getParticipants();

        Long buyerId = participants.getBuyerId();
        Long sellerId = participants.getSellerId();

        if (!participants.containsUser(userId) || buyerId == null || sellerId == null) {
            throw new DuckwhoException(ErrorCode.INVALID_CHAT_USER);
        }

        User buyer = userRepository.findById(buyerId).orElse(null);
        User seller = userRepository.findById(sellerId).orElse(null);

        Map<Long, User> userMap = new HashMap<>();
        if (buyer != null) userMap.put(buyer.getUserId(), buyer);
        if (seller != null) userMap.put(seller.getUserId(), seller);

        Optional<ChatMessage> lastMessageOpt = chatMessageRepository
                .findTopByChatRoomIdOrderBySentAtDesc(chatRoom.getId());

        Map<Long, ChatMessage> lastMessageMap = new HashMap<>();
        lastMessageOpt.ifPresent(message -> lastMessageMap.put(chatRoom.getId(), message));

        Integer unreadCount = chatRoomMetaInfo.getUnreadCount(userId);

        Map<Long, Integer> unreadCountMap = new HashMap<>();
        unreadCountMap.put(chatRoom.getId(), unreadCount);

        String articleThumbnailUrl = productImageService.getProductImageUrl(chatRoom.getArticleId());

        Map<Long, String> articleImageMap = new HashMap<>();
        articleImageMap.put(chatRoom.getArticleId(), articleThumbnailUrl);

        return chatRoomMapper.toChatRoomResponseDTO(
                chatRoom,
                chatRoomMetaInfo,
                userMap,
                lastMessageMap,
                unreadCountMap,
                articleImageMap
        );
    }

    /**
     * 사용자의 모든 채팅방의 안읽은 메시지 총 개수를 계산합니다.
     */
    public Integer getTotalUnreadCount(Long userId) {
        List<ChatRoomMetaInfo> userChatrooms = chatRoomMetaRepository
                .findByParticipantIdOrderByUpdateAtDesc(userId.toString());

        return ChatRoomMetaInfo.calculateTotalUnreadCount(userChatrooms, userId);
    }

    /**
     * 중복 채팅방 생성을 방지하기 위한 검증 로직
     */
    private void validateNewChatRoom(ChatRoomRequestDTO requestDto) {
        List<ChatRoom> chatRooms = chatRoomRepository.findByArticleId(requestDto.articleId());
        if (chatRooms.isEmpty()) {
            return;
        }

        List<Long> chatRoomIds = chatRooms.stream()
                .map(ChatRoom::getId)
                .collect(Collectors.toList());

        List<ChatRoomMetaInfo> metaInfos = chatRoomMetaRepository.findByChatRoomIdIn(chatRoomIds);

        for (ChatRoomMetaInfo metaInfo : metaInfos) {
            Participants participants = metaInfo.getParticipants();
            if (participants != null) {
                for (Long key : participants.getInfo().keySet()) {
                    ParticipantInfo participant = participants.getInfo().get(key);
                    if (participant.getUserId().equals(requestDto.buyerId()) &&
                            participant.getRole() == ParticipantRole.BUYER) {
                        throw new DuckwhoException(ErrorCode.DUPLICATE_CHAT_ROOM);
                    }
                }
            }
        }
    }

    /**
     * 사용자가 참여한 활성 채팅방 메타 정보를 조회합니다.
     */
    public List<ChatRoomMetaInfo> getConnectedChatRoomMetaInfos(Long userId) {
        List<ChatRoomMetaInfo> userChatRoomMetaInfos = chatRoomMetaRepository
                .findByParticipantsUserId(userId);
        
        if (userChatRoomMetaInfos.isEmpty()) {
            return Collections.emptyList();
        }
        
        return userChatRoomMetaInfos.stream()
                .filter(metaInfo -> metaInfo.getParticipants().getInfo().values().stream()
                        .anyMatch(participant -> participant.getIsConnected() != null
                                && participant.getIsConnected()))
                .collect(Collectors.toList());
    }

    /**
     * 채팅방 ID 목록과 사용자 ID에 해당하는 안읽은 메시지 개수 맵을 생성합니다.
     */
    public Map<Long, Integer> createUnreadCountMap(List<Long> chatRoomIds, Long userId) {
        if (chatRoomIds == null || chatRoomIds.isEmpty()) {
            return Collections.emptyMap();
        }
        
        Map<Long, Integer> unreadCountMap = new HashMap<>();
        
        List<ChatRoomMetaInfo> metaInfos = chatRoomMetaRepository.findByChatRoomIdIn(chatRoomIds);

        for (ChatRoomMetaInfo metaInfo : metaInfos) {
            unreadCountMap.put(metaInfo.getChatRoomId(), metaInfo.getUnreadCount(userId));
        }
        
        // 모든 채팅방 ID에 대해 결과가 있는지 확인하고 없으면 0 추가
        for (Long chatRoomId : chatRoomIds) {
            unreadCountMap.putIfAbsent(chatRoomId, 0);
        }
        
        return unreadCountMap;
    }
}