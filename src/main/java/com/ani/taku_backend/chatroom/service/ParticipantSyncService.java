package com.ani.taku_backend.chatroom.service;

import com.ani.taku_backend.chatroom.domain.document.ChatRoomMetaInfo;
import com.ani.taku_backend.chatroom.domain.document.ParticipantInfo;
import com.ani.taku_backend.chatroom.domain.entity.ChatRoomParticipant;
import com.ani.taku_backend.chatroom.domain.repository.ChatRoomMetaRepository;
import com.ani.taku_backend.chatroom.domain.repository.ChatRoomParticipantRepository;
import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * MySQL(ChatRoomParticipant)과 MongoDB(ParticipantInfo) 간의 참여자 정보 동기화를 담당하는 서비스
 * 두 저장소 간의 일관성을 유지하는 역할을 합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ParticipantSyncService {

    private final ChatRoomParticipantRepository participantRepository;
    private final ChatRoomMetaRepository metaRepository;
    
    /**
     * MySQL의 참여자 정보를 MongoDB로 동기화합니다.
     * 특히 연결 상태와 읽지 않은 메시지 카운트를 동기화합니다.
     * 
     * @param chatRoomId 채팅방 ID
     * @param userId 사용자 ID
     */
    @Transactional
    public void syncToMongoDB(Long chatRoomId, Long userId) {
        log.debug("MySQL -> MongoDB 동기화 시작: roomId={}, userId={}", chatRoomId, userId);
        
        // MySQL에서 참여자 정보 조회
        ChatRoomParticipant participant = participantRepository.findByChatRoomIdAndUserId(chatRoomId, userId)
                .orElseThrow(() -> new DuckwhoException(ErrorCode.INVALID_CHAT_USER));
                
        // MongoDB에서 채팅방 메타정보 조회
        ChatRoomMetaInfo metaInfo = metaRepository.findByChatRoomId(chatRoomId)
                .orElseThrow(() -> new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND));
                
        // 동기화 실행
        ParticipantInfo participantInfo = metaInfo.getParticipants().getInfo().get(userId);
        if (participantInfo != null) {
            // 연결 상태 동기화
            if (Boolean.TRUE.equals(participant.getIsConnected())) {
                participantInfo.connect();
            } else {
                participantInfo.disconnect();
            }
            
            // 읽지 않은 메시지 수 동기화
            participantInfo.setUnreadCount(participant.getUnreadCount());
            
            // 저장
            metaRepository.save(metaInfo);
            log.debug("MySQL -> MongoDB 동기화 완료: roomId={}, userId={}", chatRoomId, userId);
        } else {
            log.warn("MongoDB에서 참여자 정보를 찾을 수 없음: roomId={}, userId={}", chatRoomId, userId);
        }
    }
    
    /**
     * MongoDB의 참여자 정보를 MySQL로 동기화합니다.
     * 특히 연결 상태와 읽지 않은 메시지 카운트를 동기화합니다.
     * 
     * @param chatRoomId 채팅방 ID
     * @param userId 사용자 ID
     */
    @Transactional
    public void syncToMySQL(Long chatRoomId, Long userId) {
        log.debug("MongoDB -> MySQL 동기화 시작: roomId={}, userId={}", chatRoomId, userId);
        
        // MongoDB에서 채팅방 메타정보 조회
        ChatRoomMetaInfo metaInfo = metaRepository.findByChatRoomId(chatRoomId)
                .orElseThrow(() -> new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND));
                
        // MySQL에서 참여자 정보 조회
        ChatRoomParticipant participant = participantRepository.findByChatRoomIdAndUserId(chatRoomId, userId)
                .orElseThrow(() -> new DuckwhoException(ErrorCode.INVALID_CHAT_USER));
                
        // 동기화 실행
        ParticipantInfo participantInfo = metaInfo.getParticipants().getInfo().get(userId);
        if (participantInfo != null) {
            // 연결 상태 동기화
            participant.setConnected(Boolean.TRUE.equals(participantInfo.getIsConnected()));
            
            // 읽지 않은 메시지 수 동기화
            participant.setUnreadCount(participantInfo.getMessageStock());
            
            // 저장
            participantRepository.save(participant);
            log.debug("MongoDB -> MySQL 동기화 완료: roomId={}, userId={}", chatRoomId, userId);
        } else {
            log.warn("MongoDB에서 참여자 정보를 찾을 수 없음: roomId={}, userId={}", chatRoomId, userId);
        }
    }
} 