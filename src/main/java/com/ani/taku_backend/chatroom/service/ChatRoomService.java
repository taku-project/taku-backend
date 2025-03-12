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
import com.ani.taku_backend.chatroom.domain.repository.ChatMessageRepository;
import com.ani.taku_backend.chatroom.util.mapper.ChatRoomMapper;
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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

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
    private final ChatMessageRepository chatMessageRepository;
    
    // 새로 분리한 서비스들
    private final ChatRoomMetaService chatRoomMetaService;
    private final ChatMessageService chatMessageService;

    public static final String UNKNOWN_USER = "알 수 없음";

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

    @Transactional(readOnly = true)
    public List<ChatRoomResponseDTO> findChatRoomList(Long userId) {
        // 1. 활성 채팅방 메타 정보 조회 - 메타 서비스 사용
        List<ChatRoomMetaInfo> connectedChatRoomMetaInfos = chatRoomMetaService.getConnectedChatRoomMetaInfos(userId);
        
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
        
        // 7. 마지막 메시지 조회 및 맵 생성 - 메시지 서비스 사용
        Map<Long, ChatMessage> lastMessageMap = chatMessageService.getLastMessageMap(chatRoomIds);
        
        // 8. 안읽은 메시지 개수 맵 생성 - 메타 서비스 사용
        Map<Long, Integer> unreadCountMap = chatRoomMetaService.createUnreadCountMap(chatRoomIds, userId);
        
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

    public Integer getTotalUnreadCount(Long userId) {
        // 메타 서비스로 안읽은 메시지 총 개수 계산 위임
        return chatRoomMetaService.getTotalUnreadCount(userId);
    }
}