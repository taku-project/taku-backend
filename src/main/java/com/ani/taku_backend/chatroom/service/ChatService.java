package com.ani.taku_backend.chatroom.service;

import com.ani.taku_backend.chatroom.model.document.ChatRoomMetaInfo;
import com.ani.taku_backend.chatroom.model.document.ChatMessage;
import com.ani.taku_backend.chatroom.model.entity.ChatRoom;
import com.ani.taku_backend.chatroom.repository.ChatRoomMetaRepository;
import com.ani.taku_backend.chatroom.repository.ChatMessageRepository;
import com.ani.taku_backend.chatroom.repository.ChatRoomRepository;
import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.exception.ErrorCode;
import com.ani.taku_backend.user.repository.UserRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomMetaRepository chatRoomMetaRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;
    private final ChatAuthorizationService chatAuthorizationService;

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

    /**
     * HTTP API를 통해 메시지를 전송합니다.
     * 기존 구현과의 호환성을 위해 유지합니다.
     * WebSocket 구독자에게도 메시지를 발행합니다.
     */
    @Transactional
    public void sendMessageByWsRoomId(String wsRoomId, Long senderId, String content) {
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
        
        // WebSocket 구독자에게도 메시지 발행
        messagingTemplate.convertAndSend("/sub/chat/room/" + wsRoomId, message);
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
        
        // 2. 메시지 일괄 업데이트 (벌크 연산)
        chatMessageRepository.updateReadStatusForMessages(chatRoom.getId(), userId);
        
        // 3. 메타 정보 업데이트 (단일 업데이트)
        chatRoomMetaRepository.resetMessageStock(chatRoom.getId(), userId);

    }

}
