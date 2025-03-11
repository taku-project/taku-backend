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
import com.ani.taku_backend.chatroom.util.mapper.ChatRoomDataUtil;
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

    //TODO 여기도 매퍼 만든거룰 쓸 수 있을듯
    @Transactional(readOnly = true)
    public List<ChatRoomResponseDTO> findChatRoomList(Long userId) {
        // 1. 활성 채팅방 메타 정보 조회
        List<ChatRoomMetaInfo> connectedChatRoomMetaInfos = getConnectedChatRoomMetaInfos(userId);
        
        if (connectedChatRoomMetaInfos.isEmpty()) {
            return null;
        }
        
        // 2. 채팅방 ID 목록 추출
        List<Long> chatRoomIds = ChatRoomDataUtil.extractChatRoomIds(connectedChatRoomMetaInfos);
        
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
        List<Long> participantIds = ChatRoomDataUtil.extractParticipantIds(connectedChatRoomMetaInfos);
        
        // 6. 사용자 정보 조회
        List<User> users = userRepository.findByUserIdIn(participantIds);
        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(
                        User::getUserId,
                        user -> user,
                        (existing, replacement) -> existing
                ));
        
        // 7. 마지막 메시지 조회 및 맵 생성
        List<ChatMessage> latestMessages = chatMessageRepository.findLatestMessagesByChatRoomIds(chatRoomIds);
        Map<Long, ChatMessage> lastMessageMap = ChatRoomDataUtil.createLastMessageMap(latestMessages);
        
        // 8. 안읽은 메시지 개수 맵 생성
        Map<Long, Integer> unreadCountMap = ChatRoomDataUtil.createUnreadCountMap(chatRoomIds, userId, chatRoomMetaInfoMap);
        
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
                .map(chatRoom -> ChatRoomMapper.toChatRoomResponseDTO(
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

        // 상품 이미지 가져오기
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