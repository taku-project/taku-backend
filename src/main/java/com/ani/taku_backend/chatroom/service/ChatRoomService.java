package com.ani.taku_backend.chatroom.service;

import com.ani.taku_backend.chatroom.model.constant.ChatRoomStatus;
import com.ani.taku_backend.chatroom.model.dto.ChatRoomRequestDTO;
import com.ani.taku_backend.chatroom.model.dto.ChatRoomResponseDTO;
import com.ani.taku_backend.chatroom.model.entity.ChatRoom;
import com.ani.taku_backend.chatroom.repository.ChatRoomRepository;
import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.exception.ErrorCode;
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

    @Transactional
    public ChatRoomResponseDTO createChatRoom(ChatRoomRequestDTO requestDto) {
        validateNewChatRoom(requestDto);

        ChatRoom chatRoom = ChatRoom.builder()
                .articleId(requestDto.getArticleId())
                .buyerId(requestDto.getBuyerId())
                .sellerId(requestDto.getSellerId())
                .build();

        ChatRoom savedRoom = chatRoomRepository.save(chatRoom);
        return ChatRoomResponseDTO.of(savedRoom, requestDto.getBuyerId());
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
                .map(room -> ChatRoomResponseDTO.of(room, userId))
                .collect(Collectors.toList());
    }

    private void validateNewChatRoom(ChatRoomRequestDTO requestDto) {
        if (chatRoomRepository.existsByArticleIdAndBuyerIdAndSellerId(
                requestDto.getArticleId(),
                requestDto.getBuyerId(),
                requestDto.getSellerId())) {
            throw new DuckwhoException(ErrorCode.DUPLICATE_CHAT_ROOM);
        }
    }
}