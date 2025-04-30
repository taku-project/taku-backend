package com.ani.taku_backend.chatroom.service;

import com.ani.taku_backend.chatroom.domain.constant.JangterChatRole;
import com.ani.taku_backend.chatroom.dto.request.ChatRoomReqDTO;
import com.ani.taku_backend.chatroom.dto.response.ChatRoomResDTO;
import com.ani.taku_backend.chatroom.service.command.ChatRoomCommandService;
import com.ani.taku_backend.chatroom.service.query.ChatRoomQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 채팅방 서비스 파사드 (Facade)
 * 
 * 이 클래스는 외부 컨트롤러와 내부 서비스 사이의 파사드 역할을 합니다.
 * 실제 비즈니스 로직은 각각 ChatRoomCommandService와 ChatRoomQueryService로 위임합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomCommandService commandService;
    private final ChatRoomQueryService queryService;

    /**
     * 새로운 채팅방을 생성합니다.
     *
     * @param requestDto 채팅방 생성 요청 정보
     * @return 생성된 채팅방 정보
     */
    @Transactional
    public ChatRoomResDTO createChatRoom(ChatRoomReqDTO requestDto) {
        return  commandService.createChatRoom(requestDto);
    }
    
    /**
     * 사용자의 모든 채팅방 목록을 조회합니다.
     * 
     * @param userId 사용자 ID
     * @return 채팅방 응답 DTO 목록
     */
    @Transactional(readOnly = true)
    public List<ChatRoomResDTO> findChatRoomList(Long userId) {
        return queryService.findChatRoomList(userId);
    }
    
    /**
     * 사용자의 채팅방 목록을 페이징하여 조회합니다(무한 스크롤).
     * 
     * @param userId 사용자 ID
     * @param pageable 페이징 정보
     * @return 채팅방 응답 DTO 목록의 Slice
     */
    @Transactional(readOnly = true)
    public Slice<ChatRoomResDTO> findChatRoomListWithSlice(Long userId, Pageable pageable) {
        return queryService.findChatRoomListWithSlice(userId, pageable);
    }

    /**
     * 특정 채팅방의 정보를 조회합니다.
     *
     * @param roomId 채팅방 ID
     * @param userId 사용자 ID
     * @return 채팅방 정보
     */
    @Transactional(readOnly = true)
    public ChatRoomResDTO findChatRoom(String roomId, Long userId) {
        return queryService.findChatRoom(roomId, userId);
    }

    /**
     * 사용자의 모든 채팅방의 안읽은 메시지 총 개수를 계산합니다.
     */
    @Transactional(readOnly = true)
    public Integer getTotalUnreadCount(Long userId) {
        return queryService.getTotalUnreadCount(userId);
    }

    /**
     * 참여자의 활성화 상태를 변경합니다.
     */
    @Transactional
    public void updateParticipantActiveStatus(Long chatRoomId, Long userId, boolean active) {
        commandService.updateParticipantActiveStatus(chatRoomId, userId, active);
    }

    /**
     * 사용자의 역할별 채팅방 목록을 조회합니다.
     *
     * @param userId 사용자 ID
     * @param role 역할 (BUYER 또는 SELLER)
     * @return 채팅방 응답 DTO 목록
     */
    @Transactional(readOnly = true)
    public List<ChatRoomResDTO> findChatRoomListByRole(Long userId, JangterChatRole role) {
        return queryService.findChatRoomListByRole(userId, role);
    }
}