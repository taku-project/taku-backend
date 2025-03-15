package com.ani.taku_backend.chatroom.service;

import com.ani.taku_backend.chatroom.domain.constant.ChatRoomStatus;
import com.ani.taku_backend.chatroom.domain.constant.JangterChatRole;
import com.ani.taku_backend.chatroom.domain.document.ChatMessage;
import com.ani.taku_backend.chatroom.domain.document.ChatRoomMetaInfo;
import com.ani.taku_backend.chatroom.domain.document.ParticipantInfo;
import com.ani.taku_backend.chatroom.domain.document.Participants;
import com.ani.taku_backend.chatroom.domain.dto.request.ChatRoomRequestDTO;
import com.ani.taku_backend.chatroom.domain.dto.response.ChatRoomResponseDTO;
import com.ani.taku_backend.chatroom.domain.entity.ChatRoom;
import com.ani.taku_backend.chatroom.domain.entity.ChatRoomParticipant;
import com.ani.taku_backend.chatroom.domain.mapper.ChatRoomMapper;
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
import com.ani.taku_backend.chatroom.domain.repository.ChatRoomParticipantRepository;


import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.function.Function;

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
    private final DuckuJangterRepository duckuJangterRepository;
    private final UserRepository userRepository;
    private final ProductImageService productImageService;
    private final ChatRoomMapper chatRoomMapper;
    private final ChatService chatService;
    private final ChatRoomParticipantRepository chatRoomParticipantRepository;



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

        User buyer = userRepository.findById(requestDto.buyerId())
                .orElseThrow(() -> new DuckwhoException(ErrorCode.USER_NOT_FOUND));
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new DuckwhoException(ErrorCode.USER_NOT_FOUND));

        ChatRoom chatRoom = ChatRoom.builder()
                .articleId(requestDto.articleId())
                .build();

        ChatRoomParticipant buyerParticipant = ChatRoomParticipant.builder()
                .chatRoom(chatRoom)
                .user(buyer)
                .role(JangterChatRole.BUYER)
                .build();

        ChatRoomParticipant sellerParticipant = ChatRoomParticipant.builder()
                .chatRoom(chatRoom)
                .user(seller)
                .role(JangterChatRole.SELLER)
                .build();

        chatRoom.addParticipant(buyerParticipant);
        chatRoom.addParticipant(sellerParticipant);

        ChatRoom savedRoom = chatRoomRepository.save(chatRoom);

        ChatRoomMetaInfo metaInfo = ChatRoomMetaInfo.builder()
                .chatRoomId(savedRoom.getId())
                .build();

        metaInfo.initializeParticipants(requestDto.buyerId(), sellerId);
        chatRoomMetaRepository.save(metaInfo);

        String articleThumbnailUrl = productImageService.getProductImageUrl(requestDto.articleId());

        Map<Long, User> userMap = new HashMap<>();
        userMap.put(buyer.getUserId(), buyer);
        userMap.put(seller.getUserId(), seller);

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
     * 채팅방 관련 데이터를 MongoDB에서 한 번에 조회하는 메서드
     */
    private Map<String, Object> getChatRoomData(List<Long> chatRoomIds, Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        if (chatRoomIds == null || chatRoomIds.isEmpty()) {
            result.put("metaInfos", List.of());
            result.put("lastMessageMap", Map.of());
            result.put("unreadCountMap", Map.of());
            return result;
        }

        // 1. MongoDB에서 메타 정보 한 번에 조회
        List<ChatRoomMetaInfo> metaInfos = chatRoomMetaRepository.findMetaInfoWithLastMessages(chatRoomIds);
        
        // 2. 조회된 정보에서 필요한 데이터 추출
        Map<Long, ChatMessage> lastMessageMap = new HashMap<>();
        Map<Long, Integer> unreadCountMap = new HashMap<>();
        
        for (ChatRoomMetaInfo metaInfo : metaInfos) {
            Long chatRoomId = metaInfo.getChatRoomId();
            
            // 마지막 메시지 추출
            List<ChatMessage> messages = metaInfo.getMessages();
            if (messages != null && !messages.isEmpty()) {
                lastMessageMap.put(chatRoomId, messages.get(messages.size() - 1));
            }
            
            // 안 읽은 메시지 수 추출
            int unreadCount = metaInfo.getUnreadCount(userId);
            unreadCountMap.put(chatRoomId, unreadCount);
        }
        
        // 모든 채팅방에 대해 데이터가 있는지 확인
        for (Long chatRoomId : chatRoomIds) {
            unreadCountMap.putIfAbsent(chatRoomId, 0);
        }
        
        // 결과 맵에 저장
        result.put("metaInfos", metaInfos);
        result.put("lastMessageMap", lastMessageMap);
        result.put("unreadCountMap", unreadCountMap);
        
        return result;
    }

    /**
     * 사용자의 채팅방 목록을 조회합니다.
     */
    public List<ChatRoomResponseDTO> findChatRoomList(Long userId) {
        List<ChatRoom> chatRooms = chatRoomRepository.findChatRoomsWithParticipantsAndUsers(userId, ChatRoomStatus.ACTIVE);

        if (chatRooms.isEmpty()) {
            return List.of();
        }

        List<Long> chatRoomIds = chatRooms.stream()
                .map(ChatRoom::getId)
                .collect(Collectors.toList());

        Map<String, Object> chatRoomData = getChatRoomData(chatRoomIds, userId);
        List<ChatRoomMetaInfo> metaInfos = (List<ChatRoomMetaInfo>) chatRoomData.get("metaInfos");
        Map<Long, ChatMessage> lastMessageMap = (Map<Long, ChatMessage>) chatRoomData.get("lastMessageMap");
        Map<Long, Integer> unreadCountMap = (Map<Long, Integer>) chatRoomData.get("unreadCountMap");

        Map<Long, ChatRoomMetaInfo> metaInfoMap = metaInfos.stream()
                .collect(Collectors.toMap(
                        ChatRoomMetaInfo::getChatRoomId,
                        Function.identity(),
                        (existing, replacement) -> existing
                ));

        Map<Long, User> userMap = new HashMap<>();
        for (ChatRoom chatRoom : chatRooms) {
            User buyer = chatRoom.getBuyer();
            User seller = chatRoom.getSeller();

            if (buyer != null) userMap.put(buyer.getUserId(), buyer);
            if (seller != null) userMap.put(seller.getUserId(), seller);
        }

        List<Long> articleIds = chatRooms.stream()
                .map(ChatRoom::getArticleId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, String> articleImageMap = productImageService.getProductImageMap(articleIds);

        List<ChatRoomResponseDTO> result = chatRooms.stream()
                .map(room -> {
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
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return result;
    }

    /**
     * 특정 채팅방의 정보를 조회합니다.
     *
     * @param roomId 채팅방 ID
     * @param userId 사용자 ID
     * @return 채팅방 정보
     */
    @Transactional(readOnly = true)
    public ChatRoomResponseDTO findChatRoom(String roomId, Long userId) {

        ChatRoom chatRoom = chatRoomRepository.findByWsRoomIdWithParticipantsAndUsers(roomId)
                .orElseThrow(() -> new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        ChatRoomMetaInfo chatRoomMetaInfo = chatRoomMetaRepository.findByChatRoomId(chatRoom.getId())
                .orElseThrow(() -> new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        Participants participants = chatRoomMetaInfo.getParticipants();

        if (!participants.containsUser(userId)) {
            throw new DuckwhoException(ErrorCode.INVALID_CHAT_USER);
        }

        Map<Long, User> userMap = new HashMap<>();
        User buyer = chatRoom.getBuyer();
        User seller = chatRoom.getSeller();

        if (buyer == null || seller == null) {
            throw new DuckwhoException(ErrorCode.INVALID_CHAT_USER);
        }

        userMap.put(buyer.getUserId(), buyer);
        userMap.put(seller.getUserId(), seller);

        Map<Long, ChatMessage> lastMessageMap = chatService.getLastMessageMap(List.of(chatRoom.getId()));

        Map<Long, Integer> unreadCountMap = chatRoomMetaRepository.getUnreadCountMap(
                List.of(chatRoom.getId()), userId);

        Map<Long, String> articleImageMap = new HashMap<>();
        if (chatRoom.getArticleId() != null) {
            String imageUrl = productImageService.getProductImageUrl(chatRoom.getArticleId());
            articleImageMap.put(chatRoom.getArticleId(), imageUrl);
        }

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
                .findChatRoomMetaInfosByParticipantUserId(userId);

        if (userChatrooms == null || userChatrooms.isEmpty()) {
            return 0;
        }

        Integer totalUnread = ChatRoomMetaInfo.calculateTotalUnreadCount(userChatrooms, userId);

        return totalUnread;
    }

    /**
     * 중복 채팅방 생성을 방지하기 위한 검증 로직
     */
    private void validateNewChatRoom(ChatRoomRequestDTO requestDto) {
        List<ChatRoom> chatRooms = chatRoomRepository.findByArticleId(requestDto.articleId());
        if (chatRooms.isEmpty()) {
            return;
        }

        boolean hasExistingBuyer = chatRooms.stream()
                .filter(room -> room.getStatus() == ChatRoomStatus.ACTIVE)
                .flatMap(room -> room.getParticipants().stream())
                .anyMatch(participant ->
                        participant.getUser() != null &&
                        participant.getUser().getUserId().equals(requestDto.buyerId()) &&
                        participant.getRole() == JangterChatRole.BUYER);

        if (hasExistingBuyer) {
            throw new DuckwhoException(ErrorCode.DUPLICATE_CHAT_ROOM);
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
                            participant.getRole() == JangterChatRole.BUYER) {
                        throw new DuckwhoException(ErrorCode.DUPLICATE_CHAT_ROOM);
                    }
                }
            }
        }
    }

    /**
     * 참여자의 연결 상태를 변경합니다.
     * MongoDB에서 직접 상태를 업데이트합니다.
     *
     * @param chatRoomId 채팅방 ID
     * @param userId 사용자 ID
     * @param connected 연결 상태 (true: 연결됨, false: 연결 해제)
     */
    @Transactional
    public void updateParticipantConnectionStatus(Long chatRoomId, Long userId, boolean connected) {

        ChatRoomMetaInfo metaInfo = chatRoomMetaRepository.findByChatRoomId(chatRoomId)
                .orElseThrow(() -> new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        if (!metaInfo.getParticipants().containsUser(userId)) {
            throw new DuckwhoException(ErrorCode.INVALID_CHAT_USER);
        }
        if (connected) {
            metaInfo.getParticipants().getInfo().get(userId).connect();
        } else {
            metaInfo.getParticipants().getInfo().get(userId).disconnect();
        }

        chatRoomMetaRepository.save(metaInfo);

    }

    /**
     * 사용자의 역할별 채팅방 목록을 조회합니다. (최적화 버전)
     *
     * @param userId 사용자 ID
     * @param role 역할 (BUYER 또는 SELLER)
     * @return 채팅방 응답 DTO 목록
     */
    @Transactional(readOnly = true)
    public List<ChatRoomResponseDTO> findChatRoomListByRole(Long userId, JangterChatRole role) {
        List<ChatRoom> chatRooms = chatRoomRepository.findChatRoomsByUserIdAndRole(
                userId, role, ChatRoomStatus.ACTIVE);

        if (chatRooms.isEmpty()) {
            return Collections.emptyList();
        }

        List<ChatRoomResponseDTO> result = buildChatRoomResponseDTOs(chatRooms, userId);
        return result;
    }

    /**
     * 채팅방 엔티티 목록을 응답 DTO 목록으로 변환합니다.
     * 공통 로직을 추출하여 코드 중복을 방지합니다.
     */
    private List<ChatRoomResponseDTO> buildChatRoomResponseDTOs(List<ChatRoom> chatRooms, Long userId) {
        List<Long> chatRoomIds = chatRooms.stream()
                .map(ChatRoom::getId)
                .collect(Collectors.toList());

        List<ChatRoomMetaInfo> metaInfos = chatRoomMetaRepository.findMetaInfoWithLastMessages(chatRoomIds);

        Map<Long, ChatRoomMetaInfo> metaInfoMap = metaInfos.stream()
                .collect(Collectors.toMap(
                        ChatRoomMetaInfo::getChatRoomId,
                        Function.identity(),
                        (existing, replacement) -> existing
                ));

        Map<Long, User> userMap = new HashMap<>();
        for (ChatRoom chatRoom : chatRooms) {
            User buyer = chatRoom.getBuyer();
            User seller = chatRoom.getSeller();

            if (buyer != null) userMap.put(buyer.getUserId(), buyer);
            if (seller != null) userMap.put(seller.getUserId(), seller);
        }

        Map<Long, ChatMessage> lastMessageMap = chatService.getLastMessageMap(chatRoomIds);

        Map<Long, Integer> unreadCountMap = chatRoomMetaRepository.getUnreadCountMap(chatRoomIds, userId);

        List<Long> articleIds = chatRooms.stream()
                .map(ChatRoom::getArticleId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, String> articleImageMap = productImageService.getProductImageMap(articleIds);

        List<ChatRoomResponseDTO> result = chatRooms.stream()
                .map(room -> {
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
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return result;
    }

}