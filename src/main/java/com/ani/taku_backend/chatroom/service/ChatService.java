package com.ani.taku_backend.chatroom.service;

import com.ani.taku_backend.chatroom.domain.document.ChatRoomMetaInfo;
import com.ani.taku_backend.chatroom.domain.document.ChatMessage;
import com.ani.taku_backend.chatroom.domain.dto.response.ChatMessageListResponseDTO;
import com.ani.taku_backend.chatroom.domain.dto.response.ChatMessageResponseDTO;
import com.ani.taku_backend.chatroom.domain.entity.ChatRoom;
import com.ani.taku_backend.chatroom.domain.repository.ChatRoomMetaRepository;
import com.ani.taku_backend.chatroom.domain.repository.ChatMessageRepository;
import com.ani.taku_backend.chatroom.domain.repository.ChatRoomRepository;
import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Limit;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.Optional;
import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomMetaRepository chatRoomMetaRepository;

    /**
     * WebSocket을 통해 받은 메시지를 처리하고 저장합니다.
     */
    @Transactional
    public ChatMessage saveAndProcessMessage(String wsRoomId, Long senderId, String content) {
        // 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findByWsRoomId(wsRoomId)
                .orElseThrow(() -> new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        // 메시지 생성 및 저장
        ChatMessage message = ChatMessage.of(chatRoom.getId(), chatRoom.getArticleId(), senderId, content);
        chatMessageRepository.save(message);

        // 채팅방 메타 정보 조회 및 업데이트
        ChatRoomMetaInfo chatRoomMetaInfo = chatRoomMetaRepository.findByChatRoomId(chatRoom.getId())
                .orElseThrow(() -> new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        // 도메인 모델에 비즈니스 로직 위임
        chatRoomMetaInfo.handleNewMessage(message.getId(), senderId);
        chatRoomMetaRepository.save(chatRoomMetaInfo);
        
        return message;
    }

    @Transactional
    public void leaveRoomByWsRoomId(String wsRoomId, Long userId) {
        // 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findByWsRoomId(wsRoomId)
                .orElseThrow(() -> new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        // 채팅방 메타 정보 조회
        ChatRoomMetaInfo chatRoomMetaInfo = chatRoomMetaRepository.findById(String.valueOf(chatRoom.getId()))
                .orElseThrow(() -> new DuckwhoException(ErrorCode.DUPLICATE_CHAT_ROOM));

        // 사용자 검증
        if(!chatRoomMetaInfo.getParticipants().containsUser(userId)){
            throw new DuckwhoException(ErrorCode.INVALID_CHAT_USER);
        }

        // 도메인 모델에 비즈니스 로직 위임
        chatRoomMetaInfo.getParticipants().setDisconnected(userId);
        chatRoomMetaInfo.checkAndDeactivate();

        chatRoomMetaRepository.save(chatRoomMetaInfo);
        
        // 모든 참여자가 나갔으면 채팅방 비활성화
        if (chatRoomMetaInfo.getParticipants().isAllDisconnected()) {
            chatRoom.deactivate();
            chatRoomRepository.save(chatRoom);
        }
    }

    @Transactional
    public void markMessagesAsReadByWsRoomId(String wsRoomId, Long userId) {
        // 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findByWsRoomId(wsRoomId)
                .orElseThrow(() -> new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND));
        
        // 메시지 일괄 업데이트
        chatMessageRepository.updateReadStatusForMessages(chatRoom.getId(), userId);
        
        // 메타 정보의 안 읽은 메시지 카운터 초기화
        chatRoomMetaRepository.resetMessageStock(chatRoom.getId(), userId);
    }
    
    /**
     * 채팅 메시지 읽음 상태를 비동기적으로 업데이트합니다.
     * 데이터베이스 작업을 별도 스레드에서 처리하여 응답 시간을 개선합니다.
     */
    @Async
    @Transactional
    public void markMessagesAsReadAsync(String wsRoomId, Long userId) {
        log.debug("비동기 읽음 상태 업데이트 시작: roomId={}, userId={}", wsRoomId, userId);
        markMessagesAsReadByWsRoomId(wsRoomId, userId);
        log.debug("비동기 읽음 상태 업데이트 완료: roomId={}, userId={}", wsRoomId, userId);
    }

    /**
     * 채팅방의 메시지 이력을 조회합니다.
     * 무한 스크롤을 위해 messageId 이전의 메시지를 조회합니다.
     *
     * @param wsRoomId 채팅방 ID
     * @param messageId 기준 메시지 ID (null인 경우 최신 메시지부터 조회)
     * @param limit 조회할 메시지 개수
     * @return 메시지 목록과 무한 스크롤 정보
     */
    @Transactional(readOnly = true)
    public ChatMessageListResponseDTO getChatMessages(String wsRoomId, String messageId, int limit) {
        // 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findByWsRoomId(wsRoomId)
                .orElseThrow(() -> new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        Long chatRoomId = chatRoom.getId();
        List<ChatMessage> messages;

        // 추가 페이지 체크를 위해 요청 개수보다 1개 더 조회
        int queryLimit = limit + 1;

        // 초기 로딩 또는 스크롤 로딩에 따라 다른 조회 방식 사용
        if (messageId == null || messageId.isBlank()) {
            messages = chatMessageRepository.findByChatRoomIdOrderBySentAtDesc(chatRoomId, Limit.of(queryLimit));
        } else {
            Optional<ChatMessage> referenceMessage = chatMessageRepository.findById(messageId);

            if (referenceMessage.isEmpty()) {
                throw new DuckwhoException(ErrorCode.CHAT_MESSAGE_NOT_FOUND);
            }

            LocalDateTime referenceSentAt = referenceMessage.get().getSentAt();
            messages = chatMessageRepository.findByChatRoomIdAndSentAtBeforeOrderBySentAtDesc(
                    chatRoomId, referenceSentAt, Limit.of(queryLimit));
        }

        // 추가 페이지 존재 여부 확인
        boolean hasMore = messages.size() > limit;

        // 요청 개수만큼만 반환
        if (hasMore) {
            messages = messages.subList(0, limit);
        }

        // 시간순 정렬
        Collections.reverse(messages);

        // 도메인 모델에 DTO 변환 위임
        List<ChatMessageResponseDTO> responseDTOs = ChatMessage.toResponseDTOList(messages);

        return ChatMessageListResponseDTO.of(responseDTOs, hasMore);
    }

    /**
     * 사용자가 특정 채팅방에 접근할 수 있는 권한이 있는지 검증합니다.
     *
     * @param wsRoomId 채팅방 WebSocket ID
     * @param userId 사용자 ID
     * @throws DuckwhoException 채팅방이 존재하지 않거나 사용자가 채팅방에 접근할 권한이 없는 경우
     */
    @Transactional(readOnly = true)
    public void validateChatRoomAccess(String wsRoomId, Long userId) {
        // 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findByWsRoomId(wsRoomId)
                .orElseThrow(() -> new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        // 채팅방 메타 정보 조회
        ChatRoomMetaInfo chatRoomMetaInfo = chatRoomMetaRepository.findByChatRoomId(chatRoom.getId())
                .orElseThrow(() -> new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        // 사용자 접근 권한 검증
        if (!chatRoomMetaInfo.getParticipants().containsUser(userId)) {
            throw new DuckwhoException(ErrorCode.INVALID_CHAT_USER);
        }
    }

}


