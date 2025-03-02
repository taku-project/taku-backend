package com.ani.taku_backend.chatroom.controller;

import com.ani.taku_backend.chatroom.model.document.ChatMessage;
import com.ani.taku_backend.chatroom.model.dto.ChatMessageRequestDTO;
import com.ani.taku_backend.chatroom.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatMessageController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;

    /**
     * /pub/chat/message 경로로 들어오는 메시지를 처리합니다.
     * 메시지를 저장하고 해당 채팅방 구독자들에게 브로드캐스팅합니다.
     */
    @MessageMapping("/chat/message")
    public void handleMessage(@Payload ChatMessageRequestDTO messageRequest) {
        log.info("Received message: {}", messageRequest);
        
        // 메시지 저장 및 필요한 처리 수행
        ChatMessage savedMessage = chatService.saveAndProcessMessage(
            messageRequest.getRoomId(), 
            messageRequest.getSenderId(), 
            messageRequest.getContent()
        );
        
        // 구독자들에게 메시지 브로드캐스팅
        messagingTemplate.convertAndSend("/sub/chat/room/" + messageRequest.getRoomId(), savedMessage);
    }
} 