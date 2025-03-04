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

    @EventListener
    public void connectHandler(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        accessor.getSessionId();
        log.info("Connected to session {}", accessor.getSessionId());
        log.info("total session:{}",sessions.size());

    }

    @EventListener
    public void disconnectHandler(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        sessions.remove(accessor.getSessionId());
        log.info("DisConnected to session {}", accessor.getSessionId());
        log.info("total session:{}",sessions.size());

    }
}