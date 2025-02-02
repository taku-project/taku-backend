package com.ani.taku_backend.chatroom.service;

import com.ani.taku_backend.chatroom.model.document.ChatroomMetaInfo;
import com.ani.taku_backend.chatroom.model.document.ParticipantInfo;
import com.ani.taku_backend.chatroom.model.dto.ChatRoomRequestDTO;
import com.ani.taku_backend.chatroom.model.dto.ChatRoomResponseDTO;
import com.ani.taku_backend.chatroom.model.entity.ChatRoom;
import com.ani.taku_backend.chatroom.repository.ChatRoomRepository;
import com.ani.taku_backend.chatroom.repository.ChatroomMetaRepository;
import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.exception.ErrorCode;
import com.ani.taku_backend.user.model.entity.User;
import com.ani.taku_backend.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 채팅방 관련 비즈니스 로직을 처리하는 서비스
 * 채팅방의 생성, 조회, 메시지 카운트 관리 등을 담당합니다.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatroomMetaRepository chatroomMetaRepository;
    private final UserRepository userRepository;
    private final EntityManager entityManager;

    /**
     * 새로운 채팅방을 생성합니다.
     * 채팅방 생성 시 중복 검증을 수행하고, 채팅방 메타 정보도 함께 초기화합니다.
     *
     * @param requestDto 채팅방 생성 요청 정보 (상품ID, 구매자ID, 판매자ID)
     * @return 생성된 채팅방 정보
     * @throws DuckwhoException 중복된 채팅방이 존재하거나 사용자를 찾을 수 없는 경우
     */
    @Transactional
    public ChatRoomResponseDTO createChatRoom(ChatRoomRequestDTO requestDto) {
        validateNewChatRoom(requestDto);

        // 구매자와 판매자 정보를 단일 쿼리로 조회
        String jpql = """
        SELECT DISTINCT u FROM User u 
        WHERE u.userId IN (:buyerId, :sellerId)
        """;
        List<User> users = entityManager.createQuery(jpql, User.class)
                .setParameter("buyerId", requestDto.buyerId())
                .setParameter("sellerId", requestDto.sellerId())
                .getResultList();

        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getUserId, u -> u));

        User buyer = userMap.get(requestDto.buyerId());
        User seller = userMap.get(requestDto.sellerId());

        if (buyer == null || seller == null) {
            throw new DuckwhoException(ErrorCode.USER_NOT_FOUND);
        }

        ChatRoom chatRoom = ChatRoom.builder()
                .articleId(requestDto.articleId())
                .buyerId(requestDto.buyerId())
                .sellerId(requestDto.sellerId())
                .build();

        ChatRoom savedRoom = chatRoomRepository.save(chatRoom);

        ChatroomMetaInfo metaInfo = ChatroomMetaInfo.builder()
                .chatroomId(savedRoom.getRoomId())
                .build();
        metaInfo.initializeParticipants(requestDto.buyerId(), requestDto.sellerId());
        chatroomMetaRepository.save(metaInfo);

        return ChatRoomResponseDTO.of(savedRoom, buyer, seller, requestDto.buyerId());
    }

    /**
     * 사용자가 참여한 모든 채팅방 목록을 조회합니다.
     * 단일 쿼리로 채팅방과 참여자 정보를 조회하여 N+1 문제를 방지합니다.
     * 메타 정보도 일괄 조회하여 성능을 최적화합니다.
     *
     * @param userId 조회할 사용자 ID
     * @return 사용자가 참여한 채팅방 목록 (메타 정보 포함)
     * @throws DuckwhoException 사용자 정보를 찾을 수 없는 경우
     */
    public List<ChatRoomResponseDTO> findChatRoomList(Long userId) {
        // 1. 단일 쿼리로 채팅방과 사용자 정보 모두 조회
        List<ChatRoom> chatRooms = chatRoomRepository.findAllByUserIdWithParticipants(userId);

        // 2. 메타 정보 일괄 조회 (MongoDB)
        List<String> roomIds = chatRooms.stream()
                .map(ChatRoom::getRoomId)
                .collect(Collectors.toList());

        Map<String, ChatroomMetaInfo> metaInfoMap = chatroomMetaRepository.findAllByRoomIds(roomIds)
                .stream()
                .collect(Collectors.toMap(
                        ChatroomMetaInfo::getChatroomId,
                        metaInfo -> metaInfo,
                        (existing, replacement) -> existing
                ));

        // 3. JOIN FETCH로 가져온 정보 사용
        return chatRooms.stream()
                .filter(chatRoom -> !chatRoom.hasUserLeft(userId))
                .map(chatRoom -> {
                    ChatroomMetaInfo metaInfo = metaInfoMap.get(chatRoom.getRoomId());
                    return ChatRoomResponseDTO.of(
                            chatRoom,
                            chatRoom.getBuyer(),
                            chatRoom.getSeller(),
                            userId
                    );
                })
                .collect(Collectors.toList());
    }
    /**
     * 새로운 채팅방 생성 시 중복 여부를 검증합니다.
     * 동일한 상품에 대해 동일한 구매자/판매자 간의 채팅방이 이미 존재하는지 확인합니다.
     *
     * @param requestDto 채팅방 생성 요청 정보
     * @throws DuckwhoException 중복된 채팅방이 존재하는 경우
     */
    private void validateNewChatRoom(ChatRoomRequestDTO requestDto) {
        if (chatRoomRepository.existsByArticleIdAndBuyerIdAndSellerId(
                requestDto.articleId(),
                requestDto.buyerId(),
                requestDto.sellerId())) {
            throw new DuckwhoException(ErrorCode.DUPLICATE_CHAT_ROOM);
        }
    }

    /**
     * 특정 채팅방의 상세 정보를 조회합니다.
     * 채팅방 존재 여부, 접근 권한, 채팅방 퇴장 여부를 확인합니다.
     *
     * @param roomId 조회할 채팅방 ID
     * @param userId 조회 요청한 사용자 ID
     * @return 채팅방 상세 정보
     * @throws DuckwhoException 채팅방을 찾을 수 없거나, 접근 권한이 없거나, 사용자가 나간 채팅방인 경우
     */
    public ChatRoomResponseDTO findChatRoom(String roomId, Long userId) {
        // 채팅방과 참여자 정보를 단일 쿼리로 조회
        ChatRoom chatRoom = chatRoomRepository.findByRoomIdWithParticipants(roomId)
                .orElseThrow(() -> new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        // 접근 권한 확인
        if (!chatRoom.isParticipant(userId)) {
            throw new DuckwhoException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        // 채팅방 퇴장 여부 확인
        if (chatRoom.hasUserLeft(userId)) {
            throw new DuckwhoException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        // JOIN FETCH로 이미 조회된 정보 사용
        return ChatRoomResponseDTO.of(
                chatRoom,
                chatRoom.getBuyer(),
                chatRoom.getSeller(),
                userId
        );
    }

    /**
     * 특정 채팅방의 읽지 않은 메시지 수를 조회합니다.
     * MongoDB에 저장된 메타 정보를 통해 효율적으로 조회합니다.
     *
     * @param roomId 조회할 채팅방 ID
     * @param userId 조회 요청한 사용자 ID
     * @return 읽지 않은 메시지 수
     * @throws DuckwhoException 채팅방을 찾을 수 없거나 접근 권한이 없는 경우
     */
    public Integer getChatRoomUnreadCount(String roomId, Long userId) {
        // 채팅방 메타 정보 조회
        ChatroomMetaInfo metaInfo = chatroomMetaRepository.findById(roomId)
                .orElseThrow(() -> new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        // 참여자 정보 확인
        ParticipantInfo participantInfo = metaInfo.getParticipants().getInfo().get(userId.toString());
        if (participantInfo == null) {
            throw new DuckwhoException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        return participantInfo.getMessageStock();
    }


    /**
     * 사용자의 모든 채팅방에서 읽지 않은 전체 메시지 수를 조회합니다.
     * MongoDB의 메타 정보를 활용하여 효율적으로 합산합니다.
     *
     * @param userId 조회할 사용자 ID
     * @return 전체 읽지 않은 메시지 수
     */
    public Integer getTotalUnreadCount(Long userId) {
        // 사용자가 참여한 모든 채팅방의 메타 정보 조회
        List<ChatroomMetaInfo> userChatrooms = chatroomMetaRepository
                .findByParticipantIdOrderByUpdateAtDesc(userId.toString());

        // 읽지 않은 메시지 수 합산
        return userChatrooms.stream()
                .map(chatroom -> {
                    ParticipantInfo participantInfo = chatroom.getParticipants().getInfo().get(userId.toString());
                    return participantInfo != null ? participantInfo.getMessageStock() : 0;
                })
                .reduce(0, Integer::sum);
    }
}