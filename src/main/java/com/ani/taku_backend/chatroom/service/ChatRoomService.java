package com.ani.taku_backend.chatroom.service;

import com.ani.taku_backend.chatroom.model.constant.ChatRoomStatus;
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
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatroomMetaRepository chatroomMetaRepository;
    private final UserRepository userRepository;

    @Transactional
    public ChatRoomResponseDTO createChatRoom(ChatRoomRequestDTO requestDto) {
        validateNewChatRoom(requestDto);

        ChatRoom chatRoom = ChatRoom.builder()
                .articleId(requestDto.articleId())
                .buyerId(requestDto.buyerId())
                .sellerId(requestDto.sellerId())
                .build();

        ChatRoom savedRoom = chatRoomRepository.save(chatRoom);

        // 채팅방 메타정보 생성
        ChatroomMetaInfo metaInfo = ChatroomMetaInfo.builder()
                .chatroomId(savedRoom.getRoomId())
                .build();
        metaInfo.initializeParticipants(requestDto.buyerId(), requestDto.sellerId());
        chatroomMetaRepository.save(metaInfo);

        User buyer = userRepository.findById(requestDto.buyerId())
                .orElseThrow(() -> new DuckwhoException(ErrorCode.USER_NOT_FOUND));
        User seller = userRepository.findById(requestDto.sellerId())
                .orElseThrow(() -> new DuckwhoException(ErrorCode.USER_NOT_FOUND));

        return ChatRoomResponseDTO.of(savedRoom, buyer, seller);
    }

    public List<ChatRoomResponseDTO> findChatRoomList(Long userId) {
        List<ChatRoom> buyerRooms = chatRoomRepository
                .findByStatusAndBuyerIdOrderByCreatedAtDesc(ChatRoomStatus.ACTIVE, userId);
        List<ChatRoom> sellerRooms = chatRoomRepository
                .findByStatusAndSellerIdOrderByCreatedAtDesc(ChatRoomStatus.ACTIVE, userId);

        List<ChatRoom> allRooms = new ArrayList<>();
        allRooms.addAll(buyerRooms);
        allRooms.addAll(sellerRooms);

        return allRooms.stream()
                .map(chatRoom -> {
                    User buyer = userRepository.findById(chatRoom.getBuyerId())
                            .orElseThrow(() -> new DuckwhoException(ErrorCode.USER_NOT_FOUND));
                    User seller = userRepository.findById(chatRoom.getSellerId())
                            .orElseThrow(() -> new DuckwhoException(ErrorCode.USER_NOT_FOUND));
                    return ChatRoomResponseDTO.of(chatRoom, buyer, seller);
                })
                .collect(Collectors.toList());
    }

    private void validateNewChatRoom(ChatRoomRequestDTO requestDto) {
        if (chatRoomRepository.existsByArticleIdAndBuyerIdAndSellerId(
                requestDto.articleId(),
                requestDto.buyerId(),
                requestDto.sellerId())) {
            throw new DuckwhoException(ErrorCode.DUPLICATE_CHAT_ROOM);
        }
    }

    public ChatRoomResponseDTO findChatRoom(String roomId, Long userId) {
        ChatRoom chatRoom = chatRoomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        if (!chatRoom.getBuyerId().equals(userId) && !chatRoom.getSellerId().equals(userId)) {
            throw new DuckwhoException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        User buyer = userRepository.findById(chatRoom.getBuyerId())
                .orElseThrow(() -> new DuckwhoException(ErrorCode.USER_NOT_FOUND));
        User seller = userRepository.findById(chatRoom.getSellerId())
                .orElseThrow(() -> new DuckwhoException(ErrorCode.USER_NOT_FOUND));

        return ChatRoomResponseDTO.of(chatRoom, buyer, seller);
    }

    public Integer getChatRoomUnreadCount(String roomId, Long userId) {
        ChatroomMetaInfo metaInfo = chatroomMetaRepository.findById(roomId)
                .orElseThrow(() -> new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        ParticipantInfo participantInfo = metaInfo.getParticipants().getInfo().get(userId.toString());
        if (participantInfo == null) {
            throw new DuckwhoException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        return participantInfo.getMessageStock();
    }

    public Integer getTotalUnreadCount(Long userId) {
        List<ChatroomMetaInfo> userChatrooms = chatroomMetaRepository
                .findByParticipantIdOrderByUpdateAtDesc(userId.toString());

        return userChatrooms.stream()
                .map(chatroom -> {
                    ParticipantInfo participantInfo = chatroom.getParticipants().getInfo().get(userId.toString());
                    return participantInfo != null ? participantInfo.getMessageStock() : 0;
                })
                .reduce(0, Integer::sum);
    }
}