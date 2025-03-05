package com.ani.taku_backend.chatroom.service;

import com.ani.taku_backend.chatroom.model.document.ChatRoomMetaInfo;
import com.ani.taku_backend.chatroom.repository.ChatRoomMetaRepository;
import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.exception.ErrorCode;
import com.ani.taku_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 채팅 관련 인증 및 권한 검사를 담당하는 서비스
 * 
 * 이 서비스는 WebSocket 연결 및 채팅방 접근 권한 검사와 같은
 * 인증/인가 관련 로직을 처리합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatAuthorizationService {

    private final UserRepository userRepository;
    private final ChatRoomMetaRepository chatRoomMetaRepository;
    
    /**
     * 사용자가 특정 채팅방의 참여자인지 확인합니다.
     * 
     * @param email 사용자 이메일
     * @param roomId 채팅방 ID
     * @return 참여자인 경우 true, 아닌 경우 false
     */
    public boolean isRoomParticipant(String email, Long roomId) {
        // 사용자 이메일로 사용자 ID 조회
        Long userId = userRepository.findByEmail(email)
                .map(user -> user.getUserId())
                .orElseThrow(() -> new DuckwhoException(ErrorCode.USER_NOT_FOUND));
        
        if (log.isDebugEnabled()) {
            log.debug("사용자 ID 조회 완료 - email: {}, userId: {}", email, userId);
        }
        
        // 채팅방 메타 정보 조회
        ChatRoomMetaInfo chatRoomMetaInfo = chatRoomMetaRepository.findByChatRoomId(roomId)
                .orElseThrow(() -> new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND));
        
        if (log.isDebugEnabled()) {
            log.debug("채팅방 메타 정보 조회 완료 - roomId: {}", roomId);
        }
        
        // 사용자가 채팅방 참여자인지 확인
        boolean isParticipant = chatRoomMetaInfo.getParticipants().containsUser(userId);
        
        if (log.isDebugEnabled()) {
            log.debug("채팅방 참여 여부 확인 - userId: {}, roomId: {}, isParticipant: {}", 
                    userId, roomId, isParticipant);
        }
        
        return isParticipant;
    }
} 