package com.ani.taku_backend.config;

import com.ani.taku_backend.chatroom.service.facade.ChatRoomFacadeService;
import com.ani.taku_backend.chatroom.service.facade.ChatMessageFacadeService;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import com.ani.taku_backend.user.model.dto.PrincipalUser;

/**
 * STOMP 웹소켓 이벤트 리스너.
 * 이 리스너는 다음의 목적으로 사용됩니다:
 *   - 연결/해제 이벤트 로깅
 *   - 실시간 연결된 세션 수 모니터링
 *   - 사용자 온라인/오프라인 상태 관리
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class StompEventListenerConfig {

    private final Set<String> sessions = ConcurrentHashMap.newKeySet();
    // 세션 ID와 채팅방 ID 매핑을 저장 (하나의 세션이 여러 채팅방 구독 가능)
    private final Map<String, Set<Long>> sessionToChatRooms = new ConcurrentHashMap<>();
    private final ChatRoomFacadeService chatRoomFacadeService;
    private final ChatMessageFacadeService chatMessageFacadeService;
    
    /**
     * 클라이언트가 WebSocket에 연결되었을 때 호출되는 이벤트 핸들러.
     * 이 시점에서는 아직 채팅방을 구독하지 않았으므로, 세션 정보만 저장합니다.
     * 
     * @param event 세션 연결 이벤트
     */
    @EventListener
    public void connectHandler(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        
        if (sessionId != null) {

            sessions.add(sessionId);
            log.info("STOMP 세션 연결 성공 - 세션 ID: {}", sessionId);
            log.info("현재 총 연결 세션 수: {}", sessions.size());

            sessionToChatRooms.putIfAbsent(sessionId, ConcurrentHashMap.newKeySet());
        } else {
            log.warn("STOMP 세션 연결 이벤트 발생했지만 세션 ID가 null입니다");
        }
    }
    
    /**
     * 클라이언트가 특정 채팅방을 구독할 때 호출되는 이벤트 핸들러.
     * 이 시점에서 사용자를 특정 채팅방에 "온라인" 상태로 표시합니다.
     * 
     * @param event 채널 구독 이벤트
     */
    @EventListener
    public void subscribeHandler(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        String destination = accessor.getDestination();
        
        // 채팅방 구독 패턴: /sub/chat/room/{chatRoomId}
        if (destination != null && destination.startsWith("/sub/chat/room/")) {
            try {
                // 채팅방 ID 추출
                String roomIdStr = destination.substring("/sub/chat/room/".length());
                // roomIdStr에서 추가 경로가 있는 경우 제거
                if (roomIdStr.contains("/")) {
                    roomIdStr = roomIdStr.substring(0, roomIdStr.indexOf("/"));
                }
                
                Long userId = extractUserId(accessor);
                
                if (userId != null && StringUtils.hasText(roomIdStr)) {
                    // WebSocket roomId를 실제 채팅방 ID로 변환
                    Long chatRoomId = chatMessageFacadeService.getChatRoomIdFromWsRoomId(roomIdStr);
                    
                    if (chatRoomId != null) {
                        // 세션과 채팅방 매핑 저장
                        sessionToChatRooms.get(sessionId).add(chatRoomId);
                        
                        // 참여자 상태를 "온라인"으로 변경
                        chatRoomFacadeService.updateParticipantActiveStatus(chatRoomId, userId, true);
                        log.info("사용자 온라인 상태 업데이트: 세션={}, 사용자={}, 채팅방={}", sessionId, userId, chatRoomId);
                    }
                }
            } catch (Exception e) {
                log.error("채팅방 구독 처리 중 오류 발생: sessionId={}, destination={}", sessionId, destination, e);
            }
        }
    }

    /**
     * 클라이언트가 WebSocket 연결을 종료했을 때 호출되는 이벤트 핸들러.
     * 이 시점에서 사용자를 모든 구독 채팅방에서 "오프라인" 상태로 표시합니다.
     * 
     * @param event 세션 연결 해제 이벤트
     */
    @EventListener
    public void disconnectHandler(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        
        if (sessionId != null) {

            boolean removed = sessions.remove(sessionId);
            
            Long userId = extractUserId(accessor);
            
            if (removed && userId != null) {
                log.info("STOMP 세션 연결 해제 - 세션 ID: {}, 사용자 ID: {}", sessionId, userId);
                
                // 이 세션에서 구독한 모든 채팅방에서 사용자를 오프라인 상태로 표시
                Set<Long> chatRoomIds = sessionToChatRooms.getOrDefault(sessionId, Set.of());
                
                for (Long chatRoomId : chatRoomIds) {
                    try {
                        // 참여자 상태를 "오프라인"으로 변경
                        chatRoomFacadeService.updateParticipantActiveStatus(chatRoomId, userId, false);
                        log.info("사용자 오프라인 상태 업데이트: 세션={}, 사용자={}, 채팅방={}", sessionId, userId, chatRoomId);
                    } catch (Exception e) {
                        log.error("사용자 오프라인 상태 업데이트 중 오류: 채팅방={}, 사용자={}", chatRoomId, userId, e);
                    }
                }

                sessionToChatRooms.remove(sessionId);
            } else {
                log.warn("세션 제거 실패 또는 사용자 ID 없음 - 세션 ID: {}", sessionId);
            }
            log.info("현재 총 연결 세션 수: {}", sessions.size());
        } else {
            log.warn("STOMP 세션 연결 해제 이벤트 발생했지만 세션 ID가 null입니다");
        }
    }
    
    /**
     * StompHeaderAccessor에서 사용자 ID를 추출합니다.
     * 
     * @param accessor STOMP 헤더 접근자
     * @return 사용자 ID 또는 null
     */
    private Long extractUserId(StompHeaderAccessor accessor) {
        try {
            // 인증 객체에서 사용자 ID 추출
            Authentication auth = (Authentication) accessor.getUser();
            if (auth != null && auth instanceof UsernamePasswordAuthenticationToken token) {
                Object principal = token.getPrincipal();
                
                if (principal instanceof PrincipalUser) {
                    return ((PrincipalUser) principal).getUserId();
                }
            }
        } catch (Exception e) {
            log.error("사용자 ID 추출 중 오류 발생", e);
        }
        return null;
    }
    
    /**
     * 현재 연결된 세션 수를 반환합니다.
     * 
     * @return 연결된 세션 수
     */
    public int getActiveSessionCount() {
        return sessions.size();
    }
}