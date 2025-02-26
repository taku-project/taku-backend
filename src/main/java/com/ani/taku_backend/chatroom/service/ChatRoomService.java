package com.ani.taku_backend.chatroom.service;

import com.ani.taku_backend.chatroom.model.constant.ChatRoomStatus;
import com.ani.taku_backend.chatroom.model.constant.ParticipantRole;
import com.ani.taku_backend.chatroom.model.document.ChatMessage;
import com.ani.taku_backend.chatroom.model.document.ChatRoomMetaInfo;
import com.ani.taku_backend.chatroom.model.document.ParticipantInfo;
import com.ani.taku_backend.chatroom.model.document.Participants;
import com.ani.taku_backend.chatroom.model.dto.ChatRoomRequestDTO;
import com.ani.taku_backend.chatroom.model.dto.ChatRoomResponseDTO;
import com.ani.taku_backend.chatroom.model.entity.ChatRoom;
import com.ani.taku_backend.chatroom.repository.ChatMessageRepository;
import com.ani.taku_backend.chatroom.repository.ChatRoomMetaRepository;
import com.ani.taku_backend.chatroom.repository.ChatRoomRepository;
import com.ani.taku_backend.chatroom.repository.ParticipantInfoRepository;
import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.exception.ErrorCode;
import com.ani.taku_backend.user.model.entity.User;
import com.ani.taku_backend.user.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;

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

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMetaRepository chatroomMetaRepository;
    private final DuckuJangterRepository duckuJangterRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

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
        String buyerProfileImage = buyer != null ? buyer.getProfileImg() : null;
        String sellerNickname = seller != null ? seller.getNickname() : UNKNOWN_USER;
        String sellerProfileImage = seller != null ? seller.getProfileImg() : null;

        // 새로 생성된 채팅방에는 메시지가 없으므로 null 전달
        return ChatRoomResponseDTO.of(
                savedRoom,
                requestDto.buyerId(),
                sellerId,
                buyerNickname,
                buyerProfileImage,
                sellerNickname,
                sellerProfileImage,
                null
        );
    }

    @Transactional(readOnly = true)
    public List<ChatRoomResponseDTO> findChatRoomList(Long userId) {
        // 1회만 조회
        List<ChatRoomMetaInfo> userChatRoomMetaInfos = chatroomMetaRepository
                .findByParticipantsUserId(userId);

        if(userChatRoomMetaInfos.isEmpty()) {
            return null;
        }

        // 연결된 채팅방 필터링
        List<ChatRoomMetaInfo> connectedChatRoomMetaInfos = userChatRoomMetaInfos.stream()
                .filter(metaInfo -> metaInfo.getParticipants().getInfo().values().stream()
                        .anyMatch(participant -> participant.getIsConnected() != null
                                && participant.getIsConnected()))
                .collect(Collectors.toList());

        // chatRoomIds 추출
        List<Long> chatRoomIds = connectedChatRoomMetaInfos.stream()
                .map(ChatRoomMetaInfo::getChatRoomId)
                .collect(Collectors.toList());

        // ChatRoom 정보만 한 번 더 조회
        List<ChatRoom> userChatRooms = chatRoomRepository
                .findByIdInAndStatus(chatRoomIds, ChatRoomStatus.ACTIVE);

        // 이미 가지고 있는 메타 정보를 Map으로 변환
        Map<Long, ChatRoomMetaInfo> chatRoomMetaInfoMap = connectedChatRoomMetaInfos.stream()
                .collect(Collectors.toMap(
                        ChatRoomMetaInfo::getChatRoomId,
                        metaInfo -> metaInfo
                ));

        // 모든 참여자 ID 수집
        List<Long> participantIds = connectedChatRoomMetaInfos.stream()
                .flatMap(metaInfo -> metaInfo.getParticipants().getInfo().values().stream()
                        .map(ParticipantInfo::getUserId))
                .distinct()
                .collect(Collectors.toList());

        // 한 번에 사용자 정보 조회
        List<User> users = userRepository.findByUserIdIn(participantIds);
        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getUserId, user -> user));

        // 각 채팅방의 마지막 메시지 조회를 위한 Map 생성
        Map<Long, ChatMessage> lastMessageMap = new HashMap<>();
        for (Long chatRoomId : chatRoomIds) {
            Optional<ChatMessage> lastMessage = chatMessageRepository
                    .findTopByChatRoomIdOrderBySentAtDesc(chatRoomId);
            lastMessage.ifPresent(message -> lastMessageMap.put(chatRoomId, message));
        }

        return userChatRooms.stream()
                .map(chatRoom -> {
                    ChatRoomMetaInfo chatRoomMetaInfo = chatRoomMetaInfoMap.get(chatRoom.getId());
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

                    String buyerNickname = buyer != null ? buyer.getNickname() : "알 수 없음";
                    String buyerProfileImage = buyer != null ? buyer.getProfileImg() : null;
                    String sellerNickname = seller != null ? seller.getNickname() : "알 수 없음";
                    String sellerProfileImage = seller != null ? seller.getProfileImg() : null;

                    // 마지막 메시지 가져오기
                    ChatMessage lastMessage = lastMessageMap.get(chatRoom.getId());

                    return ChatRoomResponseDTO.of(
                            chatRoom,
                            buyerId,
                            sellerId,
                            buyerNickname,
                            buyerProfileImage,
                            sellerNickname,
                            sellerProfileImage,
                            lastMessage
                    );
                })
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

        // 사용자 정보 조회
        User buyer = userRepository.findById(buyerId).orElse(null);
        User seller = userRepository.findById(sellerId).orElse(null);

        String buyerNickname = buyer != null ? buyer.getNickname() : "알 수 없음";
        String buyerProfileImage = buyer != null ? buyer.getProfileImg() : null;
        String sellerNickname = seller != null ? seller.getNickname() : "알 수 없음";
        String sellerProfileImage = seller != null ? seller.getProfileImg() : null;

        // 마지막 메시지 조회
        Optional<ChatMessage> lastMessage = chatMessageRepository
                .findTopByChatRoomIdOrderBySentAtDesc(chatRoom.getId());

        return ChatRoomResponseDTO.of(
                chatRoom,
                buyerId,
                sellerId,
                buyerNickname,
                buyerProfileImage,
                sellerNickname,
                sellerProfileImage,
                lastMessage.orElse(null)
        );
    }

    public Integer getChatRoomUnreadCount(String roomId, Long userId) {
        ChatRoom chatRoom = chatRoomRepository.findByWsRoomId(roomId)
                .orElseThrow(() -> new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        ChatRoomMetaInfo metaInfo = chatroomMetaRepository.findByChatRoomId(chatRoom.getId())
                .orElseThrow(() -> new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        Participants participants = metaInfo.getParticipants();
        boolean isParticipant = false;

        // participants 정보를 통해 사용자가 구매자나 판매자인지 확인
        for (Long key : participants.getInfo().keySet()) {
            ParticipantInfo participant = participants.getInfo().get(key);
            if (participant.getUserId().equals(userId)) {
                isParticipant = true;
                break;
            }
        }

        if (!isParticipant) {
            throw new DuckwhoException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        ParticipantInfo participantInfo = metaInfo.getParticipants().getInfo().get(userId);
        if (participantInfo == null) {
            // 참여자 정보가 없더라도 채팅방 참여자라면 0을 반환
            return 0;
        }

        return participantInfo.getMessageStock();
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