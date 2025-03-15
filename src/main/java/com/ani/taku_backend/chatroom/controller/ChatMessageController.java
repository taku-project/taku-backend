package com.ani.taku_backend.chatroom.controller;

import com.ani.taku_backend.chatroom.domain.document.ChatMessage;
import com.ani.taku_backend.chatroom.domain.dto.request.ChatMessageRequestDTO;
import com.ani.taku_backend.chatroom.domain.dto.ChatReadStatusDTO;
import com.ani.taku_backend.chatroom.domain.entity.ChatRoom;
import com.ani.taku_backend.chatroom.domain.repository.ChatRoomRepository;
import com.ani.taku_backend.chatroom.service.facade.ChatMessageFacadeService;

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
    private final ChatMessageFacadeService chatMessageFacadeService;
    private final ChatRoomRepository chatRoomRepository;

    /**
     * 클라이언트로부터 채팅 메시지를 수신하고 처리합니다.
     * /pub/chat/message 경로로 들어오는 메시지를 처리합니다.
     * 
     * @param messageRequest 클라이언트가 보낸 채팅 메시지 요청 객체
     */
    @MessageMapping("/chat/message")
    public void sendMessage(@Payload ChatMessageRequestDTO messageRequest) {
        String roomId = messageRequest.roomId();
        Long senderId = messageRequest.senderId();
        
        log.info("[채팅] 메시지 수신: roomId={}, senderId={}", roomId, senderId);
        
        try {
            chatMessageFacadeService.validateChatRoomAccess(roomId, senderId);
            
            if (log.isDebugEnabled()) {
                log.debug("[채팅] 메시지 내용: {}", messageRequest.content());
            }

            ChatMessage savedMessage = chatMessageFacadeService.saveAndProcessMessage(
                    roomId,
                    senderId,
                    messageRequest.content()
            );

            String destination = "/sub/chat/room/" + roomId;
            messagingTemplate.convertAndSend(destination, savedMessage);
            
            log.info("[채팅] 메시지 발행 완료: roomId={}, messageId={}", roomId, savedMessage.getId());
        } catch (DuckwhoException e) {
            log.error("[채팅] 메시지 처리 중 오류 발생: roomId={}, senderId={}, error={}", 
                    roomId, senderId, e.getMessage());

            messagingTemplate.convertAndSendToUser(
                senderId.toString(), 
                "/queue/errors",
                createErrorMessage(e.getErrorCode().name(), e.getMessage())
            );
        } catch (Exception e) {
            log.error("[채팅] 예상치 못한 오류 발생: roomId={}, senderId={}", roomId, senderId, e);
            

            messagingTemplate.convertAndSendToUser(
                senderId.toString(), 
                "/queue/errors", 
                createErrorMessage("INTERNAL_SERVER_ERROR", "메시지 처리 중 서버 오류가 발생했습니다.")
            );
        }
    }
    
    /**
     * 채팅방 메시지 읽음 상태 업데이트 요청을 처리합니다.
     * /pub/chat/read 경로로 들어오는 메시지를 처리합니다.
     * 
     * @param request 읽음 상태 업데이트 요청 (roomId와 userId 포함)
     */
    @MessageMapping("/chat/read")
    public void markAsRead(@Payload ChatMessageRequestDTO request) {
        String roomId = request.roomId();
        Long userId = request.senderId();
        
        log.info("[채팅] 읽음 상태 업데이트 요청: roomId={}, userId={}", roomId, userId);
        
        try {
            // 채팅방 접근 권한 검증
            chatMessageFacadeService.validateChatRoomAccess(roomId, userId);
            
            // 메시지 읽음 상태 비동기 업데이트
            chatMessageFacadeService.markMessagesAsReadAsync(roomId, userId);
            
            // 채팅방 정보 조회
            ChatRoom chatRoom = chatRoomRepository.findByWsRoomId(roomId)
                    .orElseThrow(() -> new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND));
            
            // 읽음 상태 변경 알림 전송
            ChatReadStatusDTO readStatusDTO = ChatReadStatusDTO.of(
                    chatRoom.getId(), 
                    userId
            );
            
            String destination = "/sub/chat/room/" + roomId + "/read";
            messagingTemplate.convertAndSend(destination, readStatusDTO);
            
            log.info("[채팅] 읽음 상태 알림 전송 완료: roomId={}, userId={}", roomId, userId);
        } catch (DuckwhoException e) {
            log.error("[채팅] 읽음 상태 업데이트 중 오류 발생: roomId={}, userId={}, error={}", 
                    roomId, userId, e.getMessage());

            messagingTemplate.convertAndSendToUser(
                userId.toString(), 
                "/queue/errors",
                createErrorMessage(e.getErrorCode().name(), e.getMessage())
            );
        } catch (Exception e) {
            log.error("[채팅] 예상치 못한 오류 발생: roomId={}, userId={}", roomId, userId, e);

            messagingTemplate.convertAndSendToUser(
                userId.toString(), 
                "/queue/errors", 
                createErrorMessage("INTERNAL_SERVER_ERROR", "읽음 처리 중 서버 오류가 발생했습니다.")
            );
        }
    }

    private Object createErrorMessage(String code, String message) {
        return new ErrorMessage(code, message);
    }
    

    private record ErrorMessage(String code, String message) {}
}