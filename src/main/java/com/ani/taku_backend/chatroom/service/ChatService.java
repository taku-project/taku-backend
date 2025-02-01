package com.ani.taku_backend.chatroom.service;

import com.ani.taku_backend.chatroom.model.constant.ChatRoomStatus;
import com.ani.taku_backend.chatroom.model.entity.ChatMessage;
import com.ani.taku_backend.chatroom.model.entity.ChatRoom;
import com.ani.taku_backend.chatroom.repository.ChatMessageRepository;
import com.ani.taku_backend.chatroom.repository.ChatRoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatService {

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    // 메시지 전송
    @Transactional
    public void sendMessage(Long roomId, Long senderId, String content) {
        System.out.println("roomId: " + roomId);
        // ChatRoom 조회
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Chat room not found"));

        // MongoDB에 메시지 저장
        ChatMessage message = new ChatMessage();
        message.setRoomId(chatRoom.getId());
        message.setArticleId(chatRoom.getArticleId());
        message.setSenderId(senderId);
        message.setContent(content);
        message.setSentAt(LocalDateTime.now());
        message.setRead(false); // 처음에는 읽지 않은 상태
        message.setStatus(ChatRoomStatus.ACTIVE);

        chatMessageRepository.save(message);
    }

    // 채팅방 나가기
    @Transactional
    public void leaveRoom(Long chatRoomId, Long userId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new RuntimeException("Chat room not found"));

        chatRoom.deactivate();  // 채팅방 상태를 비활성화
        chatRoomRepository.save(chatRoom);
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
