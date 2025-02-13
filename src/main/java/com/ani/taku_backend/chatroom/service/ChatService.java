package com.ani.taku_backend.chatroom.service;

import com.ani.taku_backend.chatroom.model.constant.ChatRoomStatus;
import com.ani.taku_backend.chatroom.model.document.ChatRoomMetaInfo;
import com.ani.taku_backend.chatroom.model.entity.ChatMessage;
import com.ani.taku_backend.chatroom.model.entity.ChatRoom;
import com.ani.taku_backend.chatroom.repository.ChatRoomMetaInfoRepository;
import com.ani.taku_backend.chatroom.repository.ChatMessageRepository;
import com.ani.taku_backend.chatroom.repository.ChatRoomRepository;
import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ChatService {

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private ChatRoomMetaInfoRepository chatRoomMetaInfoRepository;

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

    private void updateLastMessageId(Long roomId, String messageId) {
        // roomId에 해당하는 ChatRoomMetaInfo 엔티티를 조회
        Optional<ChatRoomMetaInfo> chatRoomMetaInfoOpt = chatRoomMetaInfoRepository.findById(roomId);

        if (chatRoomMetaInfoOpt.isPresent()) {
            ChatRoomMetaInfo chatRoomMetaInfo = chatRoomMetaInfoOpt.get();

            // 마지막 메시지 ID를 새로 작성된 메시지의 ID로 업데이트
            chatRoomMetaInfo.setLastMessageId(messageId);

            // 갱신된 데이터를 DB에 저장
            chatRoomMetaInfoRepository.save(chatRoomMetaInfo);
        } else {
            // 해당 roomId에 해당하는 채팅방이 존재하지 않으면 예외 처리
            throw new RuntimeException("Chat room meta info not found for roomId: " + roomId);
        }
    }

    // 채팅방 나가기
    @Transactional
    public void leaveRoom(Long chatRoomId, Long userId) {

        ChatRoomMetaInfo chatRoomMetaInfo = chatRoomMetaInfoRepository.findById(chatRoomId)
                .orElseThrow(() -> new DuckwhoException(ErrorCode.DUPLICATE_CHAT_ROOM));

        if(!chatRoomMetaInfo.getParticipants().containsUser(userId)){
            throw new DuckwhoException(ErrorCode.INVALID_CHAT_USER);
        }

        chatRoomMetaInfo.getParticipants().setDisconnected(userId);
        chatRoomMetaInfo.checkAndDeactivate();

        chatRoomMetaInfoRepository.save(chatRoomMetaInfo);
        if (!chatRoomMetaInfo.isActive()) {
            chatRoomRepository.findById(chatRoomId).ifPresent(chatRoom -> {
                chatRoom.deactivate();
                chatRoomRepository.save(chatRoom);
            });
        }
    }

    // 메시지 읽음 상태 업데이트
    @Transactional
    public void markMessagesAsRead(Long chatRoomId,  Long userId) {

        // MongoDB에 저장된 메시지의 읽음 상태를 업데이트
        List<ChatMessage> messages = chatMessageRepository.findByRoomId(chatRoomId);
        System.out.println(messages.size());
        for (ChatMessage message : messages) {
            if (!message.getSenderId().equals(userId)) {
                message.setRead(true);
                chatMessageRepository.save(message);
            }
        }
    }



}
