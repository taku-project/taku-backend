package com.ani.taku_backend.config;

import com.ani.taku_backend.chatroom.StompHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
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

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")  // WebSocket 엔드포인트
                //.setAllowedOrigins(prodFrontUrl,devFrontUrl)
                .setAllowedOrigins("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 채팅, 알림 등 실시간 메시지를 위한 브로커 설정
        registry.setApplicationDestinationPrefixes("/pub");
        registry.enableSimpleBroker("/sub");
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.setMessageSizeLimit(160 * 64 * 1024)
                .setSendTimeLimit(20 * 10000)
                .setSendBufferSizeLimit(3 * 512 * 1024);
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