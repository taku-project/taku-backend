package com.ani.taku_backend.chatroom.domain.repository;

import com.ani.taku_backend.chatroom.domain.constant.ChatRoomStatus;

import com.ani.taku_backend.chatroom.domain.constant.JangterChatRole;
import com.ani.taku_backend.chatroom.domain.entity.ChatRoom;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface ChatRoomRepositoryCustom {
    
    /**
     * 사용자의 특정 역할 채팅방 목록을 모든 연관 엔티티와 함께 한 번에 조회합니다.
     */
    List<ChatRoom> findChatRoomsByUserIdAndRole(Long userId, JangterChatRole role, ChatRoomStatus status);
    
    /**
     * 웹소켓 채팅방 ID로 채팅방을 조회하며 참여자와 사용자 정보를 함께 로딩합니다.
     * 
     * @param wsRoomId 웹소켓 채팅방 ID
     * @return 채팅방과 연관 데이터
     */
    Optional<ChatRoom> findByWsRoomIdWithParticipantsAndUsers(String wsRoomId);
    
    /**
     * 무한 스크롤을 위한 채팅방 목록 조회
     * 
     * @param userId 사용자 ID
     * @param pageable 페이징 정보
     * @param status 채팅방 상태
     * @return 채팅방 목록 (Slice 형태)
     */
    Slice<ChatRoom> findChatRoomsWithSlice(Long userId, Pageable pageable, ChatRoomStatus status);
} 