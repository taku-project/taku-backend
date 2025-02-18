package com.ani.taku_backend.chatroom.service;

import com.ani.taku_backend.chatroom.model.document.ChatRoomMetaInfo;
import com.ani.taku_backend.chatroom.model.document.ChatMessage;
import com.ani.taku_backend.chatroom.model.entity.ChatRoom;
import com.ani.taku_backend.chatroom.repository.ChatRoomMetaRepository;
import com.ani.taku_backend.chatroom.repository.ChatRoomMetaRepository;
import com.ani.taku_backend.chatroom.repository.ChatMessageRepository;
import com.ani.taku_backend.chatroom.repository.ChatRoomRepository;
import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

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
        // wsRoomId로 ChatRoom 조회
        ChatRoom chatRoom = chatRoomRepository.findByWsRoomId(wsRoomId)
                .orElseThrow(() -> new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        // MongoDB에 메시지 저장
        ChatMessage message = ChatMessage.of(chatRoom.getId(), chatRoom.getArticleId(), senderId, content);
        chatMessageRepository.save(message);
        
        // 메타 정보 업데이트
        updateLastMessageId(chatRoom.getId(), message.getId());
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
        ChatRoom chatRoom = chatRoomRepository.findByWsRoomId(wsRoomId)
                .orElseThrow(() -> new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        List<ChatMessage> messages = chatMessageRepository.findByChatRoomId(chatRoom.getId());
        for (ChatMessage message : messages) {
            if (!message.getSenderId().equals(userId)) {
                message.setRead(true);
                chatMessageRepository.save(message);
            }
        }
    }

}
