package com.ani.taku_backend.chatroom.controller;

import com.ani.taku_backend.chatroom.model.document.ChatMessage;
import com.ani.taku_backend.chatroom.model.dto.request.ChatMessageRequestDTO;
import com.ani.taku_backend.chatroom.model.dto.ChatReadStatusDTO;
import com.ani.taku_backend.chatroom.model.entity.ChatRoom;
import com.ani.taku_backend.chatroom.repository.ChatRoomRepository;
import com.ani.taku_backend.chatroom.service.ChatService;

import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;


/**
 * WebSocket STOMP 메시지를 처리하는 컨트롤러입니다.
 * 채팅 메시지 전송 및 읽음 상태 업데이트와 같은 실시간 통신을 처리합니다.
 */
@Controller
@Slf4j
@RequiredArgsConstructor
public class ChatMessageController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;
    private final ChatRoomRepository chatRoomRepository;

    /**
     * 클라이언트로부터 채팅 메시지를 수신하고 처리합니다.
     * /pub/chat/message 경로로 들어오는 메시지를 처리합니다.
     * 
     * @param messageRequest 클라이언트가 보낸 채팅 메시지 요청 객체
     */
    @MessageMapping("/chat/message")
    public void sendMessage(@Payload ChatMessageRequestDTO messageRequest) {
        log.info("채팅 메시지 수신: roomId={}, senderId={}", messageRequest.roomId(), messageRequest.senderId());
        
        if (log.isDebugEnabled()) {
            log.debug("메시지 내용: {}", messageRequest.content());
        }

        // ChatService를 통해 메시지 저장 및 처리
        ChatMessage savedMessage = chatService.saveAndProcessMessage(
                messageRequest.roomId(),
                messageRequest.senderId(),
                messageRequest.content()
        );
        
        // 해당 채팅방 구독자에게 메시지 발행
        messagingTemplate.convertAndSend("/sub/chat/room/" + messageRequest.roomId(), savedMessage);
        log.info("메시지 발행 완료: messageId={}", savedMessage.getId());
    }
    
    /**
     * 채팅방 메시지 읽음 상태 업데이트 요청을 처리합니다.
     * /pub/chat/read 경로로 들어오는 메시지를 처리합니다.
     * 
     * @param request 읽음 상태 업데이트 요청 (roomId와 userId 포함)
     */
    @MessageMapping("/chat/read")
    public void markAsRead(@Payload ChatMessageRequestDTO request) {
        log.info("메시지 읽음 상태 업데이트 요청: roomId={}, userId={}", request.roomId(), request.senderId());
        
        // 메시지 읽음 상태 비동기 업데이트
        chatService.markMessagesAsReadAsync(request.roomId(), request.senderId());
        
        // 채팅방 정보 조회
        ChatRoom chatRoom = chatRoomRepository.findByWsRoomId(request.roomId())
                .orElseThrow(() -> new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND));
        
        // 읽음 상태 변경 알림 전송
        ChatReadStatusDTO readStatusDTO = ChatReadStatusDTO.of(
                chatRoom.getId(), 
                request.senderId()
        );
        
        messagingTemplate.convertAndSend(
                "/sub/chat/room/" + request.roomId() + "/read", 
                readStatusDTO
        );
        
        log.info("읽음 상태 알림 전송 완료: roomId={}, userId={}", request.roomId(), request.senderId());
    }
}