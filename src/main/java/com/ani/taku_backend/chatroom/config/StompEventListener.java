package com.ani.taku_backend.chatroom.config;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * STOMP 웹소켓 이벤트 리스너.
 * 이 리스너는 다음의 목적으로 사용됩니다:
 *   - 연결/해제 이벤트 로깅
 *   - 실시간 연결된 세션 수 모니터링
 *   - 로그 디버깅 용도
 */
@Component
@Slf4j
public class StompEventListener {

    private final Set<String> sessions = ConcurrentHashMap.newKeySet();

    /**
     * 클라이언트가 WebSocket에 연결되었을 때 호출되는 이벤트 핸들러
     * 
     * @param event 세션 연결 이벤트
     */
    @EventListener
    public void connectHandler(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        String sessionId = accessor.getSessionId();
        
        if (sessionId != null) {
            // 세션 추가
            sessions.add(sessionId);
            log.info("STOMP 세션 연결 성공 - 세션 ID: {}", sessionId);
            log.info("현재 총 연결 세션 수: {}", sessions.size());
        } else {
            log.warn("STOMP 세션 연결 이벤트 발생했지만 세션 ID가 null입니다");
        }
    }

    /**
     * 클라이언트가 WebSocket 연결을 종료했을 때 호출되는 이벤트 핸들러
     * 
     * @param event 세션 연결 해제 이벤트
     */
    @EventListener
    public void disconnectHandler(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        
        if (sessionId != null) {
            // 세션 제거
            boolean removed = sessions.remove(sessionId);
            if (removed) {
                log.info("STOMP 세션 연결 해제 - 세션 ID: {}", sessionId);
            } else {
                log.warn("세션 제거 실패 - 존재하지 않는 세션 ID: {}", sessionId);
            }
            log.info("현재 총 연결 세션 수: {}", sessions.size());
        } else {
            log.warn("STOMP 세션 연결 해제 이벤트 발생했지만 세션 ID가 null입니다");
        }
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