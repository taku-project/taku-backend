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
import com.ani.taku_backend.chatroom.util.mapper.ChatRoomMapper;
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
import com.ani.taku_backend.chatroom.util.ChatDateTimeFormatter;

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
        // 1. 장터 게시글 존재 여부 확인
        DuckuJangter product = duckuJangterRepository.findById(requestDto.articleId())
                .orElseThrow(() -> new DuckwhoException(ErrorCode.NOT_FOUND_POST));

        // 2. 판매자 정보 가져오기
        Long sellerId = product.getUser().getUserId();

        // 3. 구매자가 판매자와 동일인물이면 안 됨
        if (sellerId.equals(requestDto.buyerId())) {
            throw new DuckwhoException(ErrorCode.INVALID_CHAT_USER);
        }

        // 4. 게시글 상태 확인 (판매중인 상태인지)
        if (product.getStatus() != ProductStatus.FOR_SALE) {
            throw new DuckwhoException(ErrorCode.INVALID_PRODUCT_STATUS);
        }

        validateNewChatRoom(requestDto);

        ChatRoom chatRoom = ChatRoom.builder()
                .articleId(requestDto.articleId())
                .build();

        ChatRoom savedRoom = chatRoomRepository.save(chatRoom);

        // 채팅방 메타정보 생성
        ChatRoomMetaInfo metaInfo = ChatRoomMetaInfo.builder()
                .chatRoomId(savedRoom.getId())
                .build();
        // 실제 판매자 ID 사용
        metaInfo.initializeParticipants(requestDto.buyerId(), sellerId);
        chatRoomMetaRepository.save(metaInfo);
        
        // 사용자 정보 조회
        User buyer = userRepository.findById(requestDto.buyerId()).orElse(null);
        User seller = userRepository.findById(sellerId).orElse(null);

        // 상품 이미지 가져오기 - ProductImageService 사용
        String articleThumbnailUrl = productImageService.getProductImageUrl(requestDto.articleId());

        // MapStruct 매퍼를 사용하여 DTO 생성
        return chatRoomMapper.toChatRoomResponseDTOWithProfileImages(
                savedRoom,
                buyer,
                seller,
                Optional.empty(),
                0,
                articleThumbnailUrl
        );
    }

    /**
     * 사용자의 채팅방 목록을 조회합니다.
     * @param userId 사용자 ID
     * @return 채팅방 응답 DTO 목록
     */
    @Transactional(readOnly = true)
    public List<ChatRoomResponseDTO> findChatRoomListOptimized(Long userId) {
        // 1.  채팅방 기본 정보를 조회
        List<ChatRoomDetailDTO> chatRooms = chatRoomRepository
                .findChatRoomsWithDetailsForUser(userId, ChatRoomStatus.ACTIVE);
        
        if (chatRooms.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 2. 채팅방 ID 목록 추출
        List<Long> chatRoomIds = chatRooms.stream()
                .map(ChatRoomDetailDTO::getId)
                .collect(Collectors.toList());
        
        // 3. 채팅방 메타 정보 조회
        List<ChatRoomMetaInfo> metaInfos = chatRoomMetaRepository.findByChatRoomIdIn(chatRoomIds);
        Map<Long, ChatRoomMetaInfo> metaInfoMap = metaInfos.stream()
                .collect(Collectors.toMap(
                        ChatRoomMetaInfo::getChatRoomId,
                        metaInfo -> metaInfo,
                        (existing, replacement) -> existing
                ));
        
        // 4. 참여자 ID 목록 추출
        List<Long> participantIds = metaInfos.stream()
                .filter(meta -> meta.getParticipants() != null)
                .flatMap(meta -> meta.getParticipants().getAllParticipantIds().stream())
                .distinct()
                .collect(Collectors.toList());
        
        // 5. 사용자 정보 조회 (
        List<User> users = userRepository.findByUserIdIn(participantIds);
        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(
                        User::getUserId,
                        user -> user,
                        (existing, replacement) -> existing
                ));
        
        // 6. 마지막 메시지 정보 조회
        Map<Long, ChatMessage> lastMessageMap = chatService.getLastMessageMap(chatRoomIds);
        
        // 7. 안읽은 메시지 수 조회
        Map<Long, Integer> unreadCountMap = createUnreadCountMap(chatRoomIds, userId);
        
        // 8. 상품 이미지 URL 조회
        List<Long> articleIds = chatRooms.stream()
                .map(ChatRoomDetailDTO::getArticleId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        
        Map<Long, String> articleImageMap = productImageService.getProductImageMap(articleIds);
        

        return chatRooms.stream()
                .map(dto -> {
                    ChatRoomMetaInfo metaInfo = metaInfoMap.get(dto.getId());
                    if (metaInfo == null || metaInfo.getParticipants() == null) {
                        return null;
                    }
                    
                    // 구매자/판매자 정보 설정
                    Long buyerId = metaInfo.getParticipants().getBuyerId();
                    Long sellerId = metaInfo.getParticipants().getSellerId();
                    User buyer = userMap.get(buyerId);
                    User seller = userMap.get(sellerId);
                    String buyerNickname = buyer != null ? buyer.getNickname() : UNKNOWN_USER;
                    String sellerNickname = seller != null ? seller.getNickname() : UNKNOWN_USER;
                    String buyerProfileImageUrl = buyer != null ? buyer.getProfileImg() : null;
                    String sellerProfileImageUrl = seller != null ? seller.getProfileImg() : null;

                    ChatMessage lastMessage = lastMessageMap.get(dto.getId());
                    String lastMessageContent = lastMessage != null ? lastMessage.getContent() : null;
                    String lastMessageTime = ChatDateTimeFormatter.formatMessageTime(
                            lastMessage != null ? lastMessage.getSentAt() : null);
                    Long lastMessageSenderId = lastMessage != null ? lastMessage.getSenderId() : null;

                    Integer unreadCount = unreadCountMap.getOrDefault(dto.getId(), 0);

                    String articleThumbnailUrl = articleImageMap.get(dto.getArticleId());

                    return new ChatRoomResponseDTO(
                            dto.getId(),
                            dto.getRoomId(),
                            dto.getArticleId(),
                            buyerId,
                            sellerId,
                            dto.getCreatedAt(),
                            buyerNickname,
                            sellerNickname,
                            lastMessageContent,
                            lastMessageTime,
                            lastMessageSenderId,
                            unreadCount,
                            articleThumbnailUrl,
                            buyerProfileImageUrl,
                            sellerProfileImageUrl
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
        // 1. 활성 채팅방 메타 정보 조회
        List<ChatRoomMetaInfo> connectedChatRoomMetaInfos = getConnectedChatRoomMetaInfos(userId);
        
        if (connectedChatRoomMetaInfos.isEmpty()) {
            return null;
        }
        
        // 2. 채팅방 ID 목록 추출
        List<Long> chatRoomIds = connectedChatRoomMetaInfos.stream()
                .map(ChatRoomMetaInfo::getChatRoomId)
                .collect(Collectors.toList());
        
        if (chatRoomIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 3. 채팅방 정보 조회
        List<ChatRoom> userChatRooms = chatRoomRepository
                .findByIdInAndStatusOptimized(chatRoomIds, ChatRoomStatus.ACTIVE);
        
        // 4. 메타 정보 맵 생성
        Map<Long, ChatRoomMetaInfo> chatRoomMetaInfoMap = connectedChatRoomMetaInfos.stream()
                .collect(Collectors.toMap(
                        ChatRoomMetaInfo::getChatRoomId,
                        metaInfo -> metaInfo,
                        (existing, replacement) -> existing
                ));
        
        // 5. 참여자 ID 목록 추출
        List<Long> participantIds = connectedChatRoomMetaInfos.stream()
                .flatMap(metaInfo -> metaInfo.getParticipants().getInfo().values().stream()
                        .map(ParticipantInfo::getUserId))
                .distinct()
                .collect(Collectors.toList());
        
        // 6. 사용자 정보 조회
        List<User> users = userRepository.findByUserIdIn(participantIds);
        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(
                        User::getUserId,
                        user -> user,
                        (existing, replacement) -> existing
                ));
        
        // 7. 마지막 메시지 조회 및 맵 생성
        Map<Long, ChatMessage> lastMessageMap = chatService.getLastMessageMap(chatRoomIds);
        
        // 8. 안읽은 메시지 개수 맵 생성
        Map<Long, Integer> unreadCountMap = createUnreadCountMap(chatRoomIds, userId);
        
        // 9. 상품 ID 목록 추출
        List<Long> articleIds = userChatRooms.stream()
                .map(ChatRoom::getArticleId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        
        // 10. 상품 이미지 맵 생성
        Map<Long, String> articleImageMap = productImageService.getProductImageMap(articleIds);
        
        // 11. 채팅방 DTO 매핑 및 반환
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

        // 도메인 모델의 메서드를 활용하여 구매자와 판매자 ID 조회
        Long buyerId = participants.getBuyerId();
        Long sellerId = participants.getSellerId();

        if (!participants.containsUser(userId) || buyerId == null || sellerId == null) {
            throw new DuckwhoException(ErrorCode.INVALID_CHAT_USER);
        }

        User buyer = userRepository.findById(buyerId).orElse(null);
        User seller = userRepository.findById(sellerId).orElse(null);

        // 마지막 메시지 조회
        Optional<ChatMessage> lastMessage = chatMessageRepository
                .findTopByChatRoomIdOrderBySentAtDesc(chatRoom.getId());

        // 안읽은 메시지 개수는 도메인 모델 메서드 사용
        Integer unreadCount = chatRoomMetaInfo.getUnreadCount(userId);

        // 상품 이미지 가져오기
        String articleThumbnailUrl = productImageService.getProductImageUrl(chatRoom.getArticleId());

        // ChatRoomMapper를 사용하여 DTO 생성
        return chatRoomMapper.toChatRoomResponseDTOWithProfileImages(
                chatRoom,
                buyer,
                seller,
                lastMessage,
                unreadCount,
                articleThumbnailUrl
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

        // 한 번에 모든 메타 정보 조회
        List<Long> chatRoomIds = chatRooms.stream()
                .map(ChatRoom::getId)
                .collect(Collectors.toList());

        List<ChatRoomMetaInfo> metaInfos = chatRoomMetaRepository.findByChatRoomIdIn(chatRoomIds);

        // 검증 로직
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
        
        // 각 채팅방의 안읽은 메시지 개수를 도메인 모델에서 조회
        for (ChatRoomMetaInfo metaInfo : metaInfos) {
            unreadCountMap.put(metaInfo.getChatRoomId(), metaInfo.getUnreadCount(userId));
        }
        
        // 모든 채팅방 ID에 대해 결과가 있는지 확인하고 없으면 0 추가
        for (Long chatRoomId : chatRoomIds) {
            unreadCountMap.putIfAbsent(chatRoomId, 0);
        }
        
        return unreadCountMap;
    }

    /**
     * 채팅방 ID로 기본 정보만 포함된 DTO를 조회합니다.
     * 
     * @param chatRoomId 채팅방 ID
     * @return 기본 정보만 포함된 채팅방 응답 DTO
     */
    public ChatRoomResponseDTO findChatRoomBasicInfo(Long chatRoomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND));
        
        // MapStruct를 활용한 기본 매핑
        return chatRoomMapper.chatRoomToBasicDTO(chatRoom);
    }
    
    /**
     * 다수의 채팅방 ID로 기본 정보만 포함된 DTO 목록을 조회합니다.
     * 
     * @param chatRoomIds 채팅방 ID 목록
     * @return 기본 정보만 포함된 채팅방 응답 DTO 목록
     */
    public List<ChatRoomResponseDTO> findChatRoomsBasicInfo(List<Long> chatRoomIds) {
        List<ChatRoom> chatRooms = chatRoomRepository.findAllById(chatRoomIds);
        
        // MapStruct를 활용한 리스트 매핑
        return chatRoomMapper.chatRoomsToBasicDTOs(chatRooms);
    }
}