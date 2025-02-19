package com.ani.taku_backend.chatroom.service;

import com.ani.taku_backend.chatroom.model.document.ChatRoomMetaInfo;
import com.ani.taku_backend.chatroom.model.document.ChatMessage;
import com.ani.taku_backend.chatroom.model.document.ParticipantInfo;
import com.ani.taku_backend.chatroom.model.entity.ChatRoom;
import com.ani.taku_backend.chatroom.repository.ChatRoomMetaRepository;
import com.ani.taku_backend.chatroom.repository.ChatMessageRepository;
import com.ani.taku_backend.chatroom.repository.ChatRoomRepository;
import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomMetaRepository chatroomMetaRepository;

    // 메시지 전송
    @Transactional
    public void sendMessage(Long roomId, Long senderId, String content) {
        // ChatRoom 조회
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Chat room not found"));

        // MongoDB에 메시지 저장
        ChatMessage message = ChatMessage.of(roomId, chatRoom.getArticleId(), senderId, content);

        chatMessageRepository.save(message);
        updateLastMessageId(roomId, message.getId());

    }

    @Transactional
    public void sendMessageByWsRoomId(String wsRoomId, Long senderId, String content) {
        if (log.isDebugEnabled()) {
            log.debug("메시지 전송 시작 - wsRoomId: {}, senderId: {}, content: {}", wsRoomId, senderId, content);
        }
        
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
        ChatRoomMetaInfo chatRoomMetaInfo = chatroomMetaRepository.findByChatRoomId(chatRoom.getId())
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

        chatroomMetaRepository.save(chatRoomMetaInfo);
        if (log.isDebugEnabled()) {
            log.debug("메시지 전송 완료");
        }
    }

    private void updateLastMessageId(Long roomId, String messageId) {
        // roomId에 해당하는 ChatRoomMetaInfo 엔티티를 조회
        Optional<ChatRoomMetaInfo> chatRoomMetaInfoOpt = chatroomMetaRepository.findByChatRoomId(roomId);

        if (chatRoomMetaInfoOpt.isPresent()) {
            ChatRoomMetaInfo chatRoomMetaInfo = chatRoomMetaInfoOpt.get();

            // 마지막 메시지 ID를 새로 작성된 메시지의 ID로 업데이트
            chatRoomMetaInfo.setLastMessageId(messageId);

            // 갱신된 데이터를 DB에 저장
            chatroomMetaRepository.save(chatRoomMetaInfo);
        } else {
            // 해당 roomId에 해당하는 채팅방이 존재하지 않으면 예외 처리
            throw new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND);
        }
    }

    // 채팅방 나가기
    @Transactional
    public void leaveRoom(Long chatRoomId, Long userId) {

        ChatRoomMetaInfo chatRoomMetaInfo = chatroomMetaRepository.findById(String.valueOf(chatRoomId))
                .orElseThrow(() -> new DuckwhoException(ErrorCode.DUPLICATE_CHAT_ROOM));

        if(!chatRoomMetaInfo.getParticipants().containsUser(userId)){
            throw new DuckwhoException(ErrorCode.INVALID_CHAT_USER);
        }

        chatRoomMetaInfo.getParticipants().setDisconnected(userId);
        chatRoomMetaInfo.checkAndDeactivate();

        chatroomMetaRepository.save(chatRoomMetaInfo);
        if (!chatRoomMetaInfo.isActive()) {
            chatRoomRepository.findById(chatRoomId).ifPresent(chatRoom -> {
                chatRoom.deactivate();
                chatRoomRepository.save(chatRoom);
            });
        }
    }

    @Transactional
    public void leaveRoomByWsRoomId(String wsRoomId, Long userId) {
        ChatRoom chatRoom = chatRoomRepository.findByWsRoomId(wsRoomId)
                .orElseThrow(() -> new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        ChatRoomMetaInfo chatRoomMetaInfo = chatroomMetaRepository.findById(String.valueOf(chatRoom.getId()))
                .orElseThrow(() -> new DuckwhoException(ErrorCode.DUPLICATE_CHAT_ROOM));

        if(!chatRoomMetaInfo.getParticipants().containsUser(userId)){
            throw new DuckwhoException(ErrorCode.INVALID_CHAT_USER);
        }

        chatRoomMetaInfo.getParticipants().setDisconnected(userId);
        chatRoomMetaInfo.checkAndDeactivate();

        chatroomMetaRepository.save(chatRoomMetaInfo);
        if (!chatRoomMetaInfo.isActive()) {
            chatRoom.deactivate();
            chatRoomRepository.save(chatRoom);
        }
    }

    // 메시지 읽음 상태 업데이트
    @Transactional
    public void markMessagesAsRead(Long chatRoomId,  Long userId) {

        // MongoDB에 저장된 메시지의 읽음 상태를 업데이트
        List<ChatMessage> messages = chatMessageRepository.findByChatRoomId(chatRoomId);
        System.out.println(messages.size());
        for (ChatMessage message : messages) {
            if (!message.getSenderId().equals(userId)) {
                message.setRead(true);
                chatMessageRepository.save(message);
            }
        }
    }

    @Transactional
    public void markMessagesAsReadByWsRoomId(String wsRoomId, Long userId) {
        if (log.isDebugEnabled()) {
            log.debug("메시지 읽음 처리 시작 - wsRoomId: {}, userId: {}", wsRoomId, userId);
        }
        
        ChatRoom chatRoom = chatRoomRepository.findByWsRoomId(wsRoomId)
                .orElseThrow(() -> new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND));
        if (log.isDebugEnabled()) {
            log.debug("채팅방 조회 완료 - chatRoomId: {}", chatRoom.getId());
        }

        List<ChatMessage> messages = chatMessageRepository.findByChatRoomId(chatRoom.getId());
        if (log.isDebugEnabled()) {
            log.debug("전체 메시지 수: {}", messages.size());
        }
        
        // 메시지 상태 로깅
        messages.forEach(message -> {
            if (log.isDebugEnabled()) {
                log.debug("메시지 상태 - messageId: {}, senderId: {}, content: {}, read: {}, sentAt: {}", 
                    message.getId(), message.getSenderId(), message.getContent(), 
                    message.getRead(), message.getSentAt());
            }
        });
        
        int unreadCount = 0;
        for (ChatMessage message : messages) {
            if (!message.getSenderId().equals(userId)) {
                unreadCount++;
                if (log.isDebugEnabled()) {
                    log.debug("안 읽은 메시지 발견 - messageId: {}, senderId: {}, content: {}, sentAt: {}", 
                        message.getId(), message.getSenderId(), message.getContent(), message.getSentAt());
                }
                message.setRead(true);
                chatMessageRepository.save(message);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("안 읽은 메시지 총 개수: {}", unreadCount);
        }
        
        // 메시지 스톡도 초기화
        ChatRoomMetaInfo chatRoomMetaInfo = chatroomMetaRepository.findByChatRoomId(chatRoom.getId())
                .orElseThrow(() -> new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND));
        
        if (log.isDebugEnabled()) {
            log.debug("채팅방 참가자 정보: {}", chatRoomMetaInfo.getParticipants().getInfo());
        }
        
        ParticipantInfo participantInfo = chatRoomMetaInfo.getParticipants().getInfo().get(userId);
        if (participantInfo != null) {
            if (log.isDebugEnabled()) {
                log.debug("메시지 스톡 초기화 전: {}", participantInfo.getMessageStock());
            }
            participantInfo.resetMessageStock();
            if (log.isDebugEnabled()) {
                log.debug("메시지 스톡 초기화 후: {}", participantInfo.getMessageStock());
            }
            chatroomMetaRepository.save(chatRoomMetaInfo);
        }
    }

}
