package com.ani.taku_backend.chatroom.domain.repository;

import com.ani.taku_backend.chatroom.domain.constant.ChatRoomStatus;
import com.ani.taku_backend.chatroom.domain.dto.ChatRoomDetailDTO;

import java.util.List;


public interface ChatRoomRepositoryCustom {
    
    /**
     * 사용자의 채팅방 목록을 상세 정보와 함께 조회합니다.
     *
     * @param userId 사용자 ID
     * @param status 채팅방 상태
     * @return 채팅방 상세 정보 DTO 리스트
     */
    List<ChatRoomDetailDTO> findChatRoomsWithDetailsForUser(Long userId, ChatRoomStatus status);
} 