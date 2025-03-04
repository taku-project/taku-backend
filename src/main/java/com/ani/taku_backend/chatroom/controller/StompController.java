package com.ani.taku_backend.chatroom.controller;

import com.ani.taku_backend.chatroom.model.dto.ChatMessageRequestDTO;
import com.ani.taku_backend.chatroom.service.ChatService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

@Controller
public class StompController {

    private final SimpMessageSendingOperations messagingTemplate;
    private final ChatService chatService;

    public StompController(SimpMessageSendingOperations messagingTemplate, ChatService chatService) {
        this.messagingTemplate = messagingTemplate;
        this.chatService = chatService;
    }

    @MessageMapping("/chat/room/{roomId}")
    public void sendMessage(@DestinationVariable String roomId, ChatMessageRequestDTO chatMessageReqDto) {
        System.out.println("메시지 수신: " + chatMessageReqDto.getContent());

        // ChatService를 통해 메시지 저장 및 처리
        // 메시지는 자동으로 구독자에게 전송됨 (/sub/chat/room/{roomId})
        chatService.saveAndProcessMessage(
                roomId,
                chatMessageReqDto.getSenderId(),
                chatMessageReqDto.getContent()
        );
    }
}