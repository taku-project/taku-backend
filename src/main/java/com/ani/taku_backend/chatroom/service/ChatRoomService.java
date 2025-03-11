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
import com.ani.taku_backend.chatroom.domain.repository.ChatRoomMetaRepository;
import com.ani.taku_backend.chatroom.domain.repository.ChatRoomRepository;
import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.exception.ErrorCode;
import com.ani.taku_backend.jangter.service.ProductImageService;
import com.ani.taku_backend.user.model.entity.User;
import com.ani.taku_backend.user.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.ani.taku_backend.jangter.model.entity.DuckuJangter;
import com.ani.taku_backend.jangter.model.enums.ProductStatus;
import com.ani.taku_backend.jangter.repository.DuckuJangterRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMetaRepository chatroomMetaRepository;
    private final DuckuJangterRepository duckuJangterRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final ProductImageService productImageService;

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
        chatroomMetaRepository.save(metaInfo);
        
        // 사용자 정보 조회
        User buyer = userRepository.findById(requestDto.buyerId()).orElse(null);
        User seller = userRepository.findById(sellerId).orElse(null);

        String buyerNickname = buyer != null ? buyer.getNickname() : UNKNOWN_USER;
        String sellerNickname = seller != null ? seller.getNickname() : UNKNOWN_USER;

        // 상품 이미지 가져오기 - ProductImageService 사용
        String articleThumbnailUrl = productImageService.getProductImageUrl(requestDto.articleId());

        // 새로 생성된 채팅방에는 메시지가 없으므로 null 전달
        return ChatRoomResponseDTO.of(
                savedRoom,
                requestDto.buyerId(),
                sellerId,
                buyerNickname,
                sellerNickname,
                null,
                0,  // 새로 생성된 채팅방에는 안읽은 메시지가 없음
                articleThumbnailUrl
        );
    }

    /**
     * 사용자가 참여한 활성 채팅방 메타 정보를 조회합니다.
     */
    private List<ChatRoomMetaInfo> getConnectedChatRoomMetaInfos(Long userId) {
        List<ChatRoomMetaInfo> userChatRoomMetaInfos = chatroomMetaRepository
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
     * 채팅방 메타 정보 목록에서 채팅방 ID 목록을 추출합니다.
     */
    private List<Long> extractChatRoomIds(List<ChatRoomMetaInfo> chatRoomMetaInfos) {
        return chatRoomMetaInfos.stream()
                .map(ChatRoomMetaInfo::getChatRoomId)
                .collect(Collectors.toList());
    }

    /**
     * 채팅방 메타 정보 목록에서 참여자 ID 목록을 추출합니다.
     */
    private List<Long> extractParticipantIds(List<ChatRoomMetaInfo> chatRoomMetaInfos) {
        return chatRoomMetaInfos.stream()
                .flatMap(metaInfo -> metaInfo.getParticipants().getInfo().values().stream()
                        .map(ParticipantInfo::getUserId))
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 채팅방 ID 목록에 해당하는 마지막 메시지 맵을 생성합니다.
     */
    private Map<Long, ChatMessage> createLastMessageMap(List<Long> chatRoomIds) {
        List<ChatMessage> latestMessages = chatMessageRepository.findLatestMessagesByChatRoomIds(chatRoomIds);
        
        Map<Long, ChatMessage> lastMessageMap = new HashMap<>();
        for (ChatMessage message : latestMessages) {
            if (!lastMessageMap.containsKey(message.getChatRoomId())) {
                lastMessageMap.put(message.getChatRoomId(), message);
            }
        }
        return lastMessageMap;
    }

    /**
     * 채팅방 ID 목록과 사용자 ID에 해당하는 안읽은 메시지 개수 맵을 생성합니다.
     */
    private Map<Long, Integer> createUnreadCountMap(List<Long> chatRoomIds, Long userId, Map<Long, ChatRoomMetaInfo> chatRoomMetaInfoMap) {
        Map<Long, Integer> unreadCountMap = new HashMap<>();
        for (Long chatRoomId : chatRoomIds) {
            ChatRoomMetaInfo metaInfo = chatRoomMetaInfoMap.get(chatRoomId);
            if (metaInfo != null && metaInfo.getParticipants() != null) {
                ParticipantInfo participantInfo = metaInfo.getParticipants().getInfo().get(userId);
                unreadCountMap.put(chatRoomId, participantInfo != null ? participantInfo.getMessageStock() : 0);
            } else {
                unreadCountMap.put(chatRoomId, 0);
            }
        }
        return unreadCountMap;
    }

    /**
     * 상품 ID 목록에 해당하는 이미지 URL 맵을 생성합니다.
     */
    private Map<Long, String> createArticleImageMap(List<Long> articleIds) {
        // ProductImageService 사용
        return productImageService.getProductImageMap(articleIds);
    }

    /**
     * 채팅방 정보를 DTO로 변환합니다.
     */
    private ChatRoomResponseDTO mapToChatRoomResponseDTO(
            ChatRoom chatRoom,
            ChatRoomMetaInfo chatRoomMetaInfo,
            Map<Long, User> userMap,
            Map<Long, ChatMessage> lastMessageMap,
            Map<Long, Integer> unreadCountMap,
            Map<Long, String> articleImageMap) {
        
        if (chatRoomMetaInfo == null || chatRoomMetaInfo.getParticipants() == null || chatRoomMetaInfo.getParticipants().getInfo() == null) {
            return null;
        }

        Participants participants = chatRoomMetaInfo.getParticipants();

        Long buyerId = 0L;
        Long sellerId = 0L;

        for(Long key : participants.getInfo().keySet()) {
            ParticipantInfo participantInfo = participants.getInfo().get(key);
            if(participantInfo.getRole() == ParticipantRole.BUYER) {
                buyerId = participantInfo.getUserId();
            } else {
                sellerId = participantInfo.getUserId();
            }
        }

        // 구매자, 판매자 정보 가져오기
        User buyer = userMap.get(buyerId);
        User seller = userMap.get(sellerId);

        String buyerNickname = buyer != null ? buyer.getNickname() : UNKNOWN_USER;
        String sellerNickname = seller != null ? seller.getNickname() : UNKNOWN_USER;

        // 마지막 메시지 가져오기
        ChatMessage lastMessage = lastMessageMap.get(chatRoom.getId());

        // 안읽은 메시지 개수 가져오기
        Integer unreadCount = unreadCountMap.get(chatRoom.getId());

        // 상품 이미지 가져오기
        String articleThumbnailUrl = articleImageMap.get(chatRoom.getArticleId());

        return ChatRoomResponseDTO.of(
                chatRoom,
                buyerId,
                sellerId,
                buyerNickname,
                sellerNickname,
                lastMessage,
                unreadCount,
                articleThumbnailUrl
        );
    }

    @Transactional(readOnly = true)
    public List<ChatRoomResponseDTO> findChatRoomList(Long userId) {
        // 1. 활성 채팅방 메타 정보 조회
        List<ChatRoomMetaInfo> connectedChatRoomMetaInfos = getConnectedChatRoomMetaInfos(userId);
        
        if (connectedChatRoomMetaInfos.isEmpty()) {
            return null;
        }
        
        // 2. 채팅방 ID 목록 추출
        List<Long> chatRoomIds = extractChatRoomIds(connectedChatRoomMetaInfos);
        
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
        List<Long> participantIds = extractParticipantIds(connectedChatRoomMetaInfos);
        
        // 6. 사용자 정보 조회
        List<User> users = userRepository.findByUserIdIn(participantIds);
        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(
                        User::getUserId,
                        user -> user,
                        (existing, replacement) -> existing
                ));
        
        // 7. 마지막 메시지 맵 생성
        Map<Long, ChatMessage> lastMessageMap = createLastMessageMap(chatRoomIds);
        
        // 8. 안읽은 메시지 개수 맵 생성
        Map<Long, Integer> unreadCountMap = createUnreadCountMap(chatRoomIds, userId, chatRoomMetaInfoMap);
        
        // 9. 상품 ID 목록 추출
        List<Long> articleIds = userChatRooms.stream()
                .map(ChatRoom::getArticleId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        
        // 10. 상품 이미지 맵 생성
        Map<Long, String> articleImageMap = createArticleImageMap(articleIds);
        
        // 11. 채팅방 DTO 매핑 및 반환
        return userChatRooms.stream()
                .map(chatRoom -> mapToChatRoomResponseDTO(
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

        List<ChatRoomMetaInfo> metaInfos = chatroomMetaRepository.findByChatRoomIdIn(chatRoomIds);

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

        ChatRoomMetaInfo chatRoomMetaInfo = chatroomMetaRepository.findByChatRoomId(chatRoom.getId())
                .orElseThrow(() -> new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        Participants participants = chatRoomMetaInfo.getParticipants();

        Long buyerId = 0L;
        Long sellerId = 0L;
        for(Long key: participants.getInfo().keySet()){
            if(participants.getInfo().get(key).getRole()==ParticipantRole.BUYER){
                buyerId = participants.getInfo().get(key).getUserId();
            }else{
                sellerId = participants.getInfo().get(key).getUserId();
            }
        }

        if (!participants.containsUser(userId)||buyerId==0L||sellerId==0L) {
            throw new DuckwhoException(ErrorCode.INVALID_CHAT_USER);
        }

        User buyer = userRepository.findById(buyerId).orElse(null);
        User seller = userRepository.findById(sellerId).orElse(null);

        String buyerNickname = buyer != null ? buyer.getNickname() : UNKNOWN_USER;
        String sellerNickname = seller != null ? seller.getNickname() : UNKNOWN_USER;

        // 마지막 메시지 조회
        Optional<ChatMessage> lastMessage = chatMessageRepository
                .findTopByChatRoomIdOrderBySentAtDesc(chatRoom.getId());

        // 안읽은 메시지 개수는 ParticipantInfo의 messageStock을 사용
        ParticipantInfo participantInfo = participants.getInfo().get(userId);
        Integer unreadCount = participantInfo != null ? participantInfo.getMessageStock() : 0;

        // 상품 이미지 가져오기 - ProductImageService 사용
        String articleThumbnailUrl = productImageService.getProductImageUrl(chatRoom.getArticleId());

        return ChatRoomResponseDTO.of(
                chatRoom,
                buyerId,
                sellerId,
                buyerNickname,
                sellerNickname,
                lastMessage.orElse(null),
                unreadCount,
                articleThumbnailUrl
        );
    }

    //TODO 삭제 예정
    public Integer getTotalUnreadCount(Long userId) {
        List<ChatRoomMetaInfo> userChatrooms = chatroomMetaRepository
                .findByParticipantIdOrderByUpdateAtDesc(userId.toString());

        return userChatrooms.stream()
                .map(chatroom -> {
                    ParticipantInfo participantInfo = chatroom.getParticipants().getInfo().get(userId);
                    return participantInfo != null ? participantInfo.getMessageStock() : 0;
                })
                .reduce(0, Integer::sum);
    }
}