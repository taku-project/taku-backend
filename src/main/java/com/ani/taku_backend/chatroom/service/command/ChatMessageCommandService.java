package com.ani.taku_backend.chatroom.service.command;

import com.ani.taku_backend.chatroom.domain.document.ChatRoomMetaInfo;
import com.ani.taku_backend.chatroom.domain.document.ChatMessage;
import com.ani.taku_backend.chatroom.domain.entity.ChatRoom;
import com.ani.taku_backend.chatroom.domain.repository.ChatRoomMetaRepository;
import com.ani.taku_backend.chatroom.domain.repository.ChatRoomRepository;
import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.exception.ErrorCode;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 채팅 메시지 관련 명령(Command) 작업을 담당하는 서비스
 * 메시지 저장, 읽음 처리, 채팅방 나가기 등 상태를 변경하는 기능을 제공합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ChatMessageCommandService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMetaRepository chatRoomMetaRepository;

    /**
     * WebSocket을 통해 받은 메시지를 처리하고 저장합니다.
     */
    public ChatMessage saveAndProcessMessage(String wsRoomId, Long senderId, String content) {
        log.debug("메시지 저장 요청: roomId={}, senderId={}", wsRoomId, senderId);

        ChatRoom chatRoom = chatRoomRepository.findByWsRoomId(wsRoomId)
                .orElseThrow(() -> new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        ChatMessage message = ChatMessage.of(chatRoom.getId(), chatRoom.getArticleId(), senderId, content);

        ChatRoomMetaInfo metaInfo = chatRoomMetaRepository.findByChatRoomId(chatRoom.getId())
                .orElseThrow(() -> new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        metaInfo.addMessage(message);
        chatRoomMetaRepository.save(metaInfo);
        
        return message;
    }

    /**
     * 사용자가 채팅방을 나갈 때 처리하는 메서드입니다.
     */
    public void leaveRoom(String wsRoomId, Long userId) {
        log.debug("채팅방 나가기 요청: wsRoomId={}, userId={}", wsRoomId, userId);

        ChatRoom chatRoom = chatRoomRepository.findByWsRoomId(wsRoomId)
                .orElseThrow(() -> new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        ChatRoomMetaInfo metaInfo = chatRoomMetaRepository.findByChatRoomId(chatRoom.getId())
                .orElseThrow(() -> new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        boolean allParticipantsLeft = metaInfo.leaveRoom(userId);
        chatRoomMetaRepository.save(metaInfo);

        if (allParticipantsLeft) {
            chatRoom.deactivate();
            chatRoomRepository.save(chatRoom);
            log.info("모든 참여자가 나가 채팅방 비활성화: chatRoomId={}", chatRoom.getId());
        }
    }

    /**
     * 채팅방의 읽지 않은 메시지를 모두 읽음 상태로 표시합니다.
     */
    public void markMessagesAsRead(String wsRoomId, Long userId) {
        log.debug("메시지 읽음 처리: wsRoomId={}, userId={}", wsRoomId, userId);

        ChatRoom chatRoom = chatRoomRepository.findByWsRoomId(wsRoomId)
                .orElseThrow(() -> new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        ChatRoomMetaInfo metaInfo = chatRoomMetaRepository.findByChatRoomId(chatRoom.getId())
                .orElseThrow(() -> new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        metaInfo.readAllMessages(userId);
        chatRoomMetaRepository.save(metaInfo);
    }

    /**
     * 비동기로 메시지 읽음 상태를 업데이트합니다.
     */
    @Async
    public void markMessagesAsReadAsync(String wsRoomId, Long userId) {
        try {
            markMessagesAsRead(wsRoomId, userId);
        } catch (Exception e) {
            log.error("Error marking messages as read: {}", e.getMessage(), e);
        }
    }
} 