package com.ani.taku_backend.chatroom.service;

import com.ani.taku_backend.chatroom.model.document.ChatRoomMetaInfo;
import com.ani.taku_backend.chatroom.model.document.ChatMessage;
import com.ani.taku_backend.chatroom.model.dto.response.ChatMessageListResponseDTO;
import com.ani.taku_backend.chatroom.model.dto.response.ChatMessageResponseDTO;
import com.ani.taku_backend.chatroom.model.entity.ChatRoom;
import com.ani.taku_backend.chatroom.repository.ChatRoomMetaRepository;
import com.ani.taku_backend.chatroom.repository.ChatMessageRepository;
import com.ani.taku_backend.chatroom.repository.ChatRoomRepository;
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
        // wsRoomId로 ChatRoom 조회
        ChatRoom chatRoom = chatRoomRepository.findByWsRoomId(wsRoomId)
                .orElseThrow(() -> new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND));
        
        if (log.isDebugEnabled()) {
            log.debug("채팅방 조회 완료 - chatRoomId: {}", chatRoom.getId());
        }

        // MongoDB에 메시지 저장
        ChatMessage message = ChatMessage.of(chatRoom.getId(), chatRoom.getArticleId(), senderId, content);
        chatMessageRepository.save(message);
        
        if (log.isDebugEnabled()) {
            log.debug("메시지 저장 완료 - messageId: {}, read: {}", message.getId(), message.getRead());
        }
        
        // 메타 정보 업데이트
        updateLastMessageId(chatRoom.getId(), message.getId());

        // 채팅방 메타 정보 조회
        ChatRoomMetaInfo chatRoomMetaInfo = chatRoomMetaRepository.findByChatRoomId(chatRoom.getId())
                .orElseThrow(() -> new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND));
                
        if (log.isDebugEnabled()) {
            log.debug("채팅방 메타 정보 조회 완료 - participants: {}", chatRoomMetaInfo.getParticipants().getInfo());
        }

        // 상대방의 메시지 스톡 증가
        chatRoomMetaInfo.getParticipants().getInfo().forEach((userId, participantInfo) -> {
            if (log.isDebugEnabled()) {
                log.debug("참가자 정보 처리 - userId: {}, role: {}, currentStock: {}, isConnected: {}", 
                        userId, participantInfo.getRole(), participantInfo.getMessageStock(), participantInfo.getIsConnected());
            }
            if (!userId.equals(senderId)) {
                participantInfo.plusMessage();
                if (log.isDebugEnabled()) {
                    log.debug("메시지 스톡 증가 - userId: {}, newStock: {}", userId, participantInfo.getMessageStock());
                }
            }
        });

        chatRoomMetaRepository.save(chatRoomMetaInfo);
        
        return message;
    }


    private void updateLastMessageId(Long roomId, String messageId) {
        // roomId에 해당하는 ChatRoomMetaInfo 엔티티를 조회
        Optional<ChatRoomMetaInfo> chatRoomMetaInfoOpt = chatRoomMetaRepository.findByChatRoomId(roomId);

        if (chatRoomMetaInfoOpt.isPresent()) {
            ChatRoomMetaInfo chatRoomMetaInfo = chatRoomMetaInfoOpt.get();

            // 마지막 메시지 ID를 새로 작성된 메시지의 ID로 업데이트
            chatRoomMetaInfo.setLastMessageId(messageId);

            // 갱신된 데이터를 DB에 저장
            chatRoomMetaRepository.save(chatRoomMetaInfo);
        } else {
            // 해당 roomId에 해당하는 채팅방이 존재하지 않으면 예외 처리
            throw new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND);
        }
    }


    @Transactional
    public void leaveRoomByWsRoomId(String wsRoomId, Long userId) {
        ChatRoom chatRoom = chatRoomRepository.findByWsRoomId(wsRoomId)
                .orElseThrow(() -> new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        ChatRoomMetaInfo chatRoomMetaInfo = chatRoomMetaRepository.findById(String.valueOf(chatRoom.getId()))
                .orElseThrow(() -> new DuckwhoException(ErrorCode.DUPLICATE_CHAT_ROOM));

        if(!chatRoomMetaInfo.getParticipants().containsUser(userId)){
            throw new DuckwhoException(ErrorCode.INVALID_CHAT_USER);
        }

        chatRoomMetaInfo.getParticipants().setDisconnected(userId);
        chatRoomMetaInfo.checkAndDeactivate();

        chatRoomMetaRepository.save(chatRoomMetaInfo);
        if (!chatRoomMetaInfo.isActive()) {
            chatRoom.deactivate();
            chatRoomRepository.save(chatRoom);
        }
    }


    @Transactional
    public void markMessagesAsReadByWsRoomId(String wsRoomId, Long userId) {
        
        // 1. 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findByWsRoomId(wsRoomId)
                .orElseThrow(() -> new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND));
        
        // 2. 메시지 일괄 업데이트
        chatMessageRepository.updateReadStatusForMessages(chatRoom.getId(), userId);
        
        // 3. 메타 정보 업데이트
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
        log.info("채팅 메시지 이력 조회 요청: roomId={}, messageId={}, limit={}", wsRoomId, messageId, limit);

        ChatRoom chatRoom = chatRoomRepository.findByWsRoomId(wsRoomId)
                .orElseThrow(() -> new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        Long chatRoomId = chatRoom.getId();
        List<ChatMessage> messages;

        int queryLimit = limit + 1;

        if (messageId == null || messageId.isBlank()) {

            messages = chatMessageRepository.findByChatRoomIdOrderBySentAtDesc(chatRoomId, Limit.of(queryLimit));
            log.debug("첫 메시지 로드: {} 개 조회됨", messages.size());
        } else {

            Optional<ChatMessage> referenceMessage = chatMessageRepository.findById(messageId);

            if (referenceMessage.isEmpty()) {
                throw new DuckwhoException(ErrorCode.CHAT_MESSAGE_NOT_FOUND);
            }

            LocalDateTime referenceSentAt = referenceMessage.get().getSentAt();
            messages = chatMessageRepository.findByChatRoomIdAndSentAtBeforeOrderBySentAtDesc(
                    chatRoomId, referenceSentAt, Limit.of(queryLimit));
            log.debug("스크롤 메시지 로드: {} 개 조회됨", messages.size());
        }

        boolean hasMore = messages.size() > limit;

        if (hasMore) {
            messages = messages.subList(0, limit);
        }

        Collections.reverse(messages);

        String oldestMessageId = messages.isEmpty() ? null : messages.get(0).getId();

        List<ChatMessageResponseDTO> responseDTOs = ChatMessageResponseDTO.listFrom(messages);

        log.info("채팅 메시지 이력 조회 완료: count={}, hasMore={}", responseDTOs.size(), hasMore);
        return ChatMessageListResponseDTO.of(responseDTOs, hasMore, oldestMessageId);
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
        log.debug("채팅방 접근 권한 검증 시작: roomId={}, userId={}", wsRoomId, userId);

        // 1. wsRoomId로 ChatRoom 조회
        ChatRoom chatRoom = chatRoomRepository.findByWsRoomId(wsRoomId)
                .orElseThrow(() -> new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        // 2. 채팅방 메타 정보 조회
        ChatRoomMetaInfo chatRoomMetaInfo = chatRoomMetaRepository.findByChatRoomId(chatRoom.getId())
                .orElseThrow(() -> new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        // 3. 사용자가 채팅방 참가자인지 확인
        if (!chatRoomMetaInfo.getParticipants().containsUser(userId)) {
            log.warn("채팅방 접근 권한 없음: roomId={}, userId={}", wsRoomId, userId);
            throw new DuckwhoException(ErrorCode.INVALID_CHAT_USER);
        }

        log.debug("채팅방 접근 권한 검증 완료: roomId={}, userId={}", wsRoomId, userId);
    }

}


