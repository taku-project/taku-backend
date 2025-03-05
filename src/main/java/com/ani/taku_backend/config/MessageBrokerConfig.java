package com.ani.taku_backend.config;

import com.ani.taku_backend.chatroom.StompHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

@Configuration
@EnableWebSocketMessageBroker
public class MessageBrokerConfig implements WebSocketMessageBrokerConfigurer {

    private final StompHandler stompHandler;

    @Value("${client.prod.front-url}")
    private String prodFrontUrl;

    @Value("${client.dev.front-url}")
    private String devFrontUrl;

    public MessageBrokerConfig(StompHandler stompHandler) {
        this.stompHandler = stompHandler;
    }

    @Bean
    public ThreadPoolTaskScheduler customMessageBrokerTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("wss-heartbeat-thread-");
        scheduler.initialize();
        return scheduler;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // SockJS를 사용하는 WebSocket 엔드포인트
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")  // 개발 환경에서는 모든 오리진 허용
                .withSockJS()
                .setDisconnectDelay(30 * 1000)  // 연결 해제 후 세션 유지 시간 (30초)
                .setHeartbeatTime(25 * 1000)    // 하트비트 주기 (25초)
                .setClientLibraryUrl("https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js");
        
        // 순수 WebSocket 엔드포인트 (SockJS 없이 직접 연결)
        registry.addEndpoint("/ws/raw")
                .setAllowedOriginPatterns("*");  // 개발 환경에서는 모든 오리진 허용
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 클라이언트가 메시지를 발행할 수 있는 목적지 접두사 (/pub/...)
        registry.setApplicationDestinationPrefixes("/pub");
        
        // 메시지 브로커가 구독 요청을 처리할 목적지 접두사 (/sub/...)
        registry.enableSimpleBroker("/sub")
                .setHeartbeatValue(new long[]{10000, 10000})  // 서버-클라이언트 하트비트 (10초)
                .setTaskScheduler(customMessageBrokerTaskScheduler());
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.setMessageSizeLimit(160 * 64 * 1024)       // 메시지 크기 제한: 약 10MB
                .setSendTimeLimit(20 * 10000)                  // 메시지 전송 시간 제한: 200초
                .setSendBufferSizeLimit(3 * 512 * 1024);       // 버퍼 크기 제한: 약 1.5MB
    }

    /**
     * 클라이언트에서 들어오는 WebSocket 메시지를 처리하는 채널 설정
     * 
     * WebSocket 요청(CONNECT, SUBSCRIBE, DISCONNECT 등)은 HTTP 헤더와 같은 메타데이터를 포함할 수 있음
     * 이 인터셉터를 통해 해당 요청을 가로채서 JWT 토큰 검증 등의 보안 처리를 수행
     * 
     * @param registration 채널 등록 객체
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompHandler);
    }
}