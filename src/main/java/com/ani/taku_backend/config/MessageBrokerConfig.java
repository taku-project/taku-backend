package com.ani.taku_backend.config;

import com.ani.taku_backend.chatroom.StompHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.messaging.converter.DefaultContentTypeResolver;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.server.HandshakeInterceptor;

@Configuration
@EnableWebSocketMessageBroker
@Slf4j
public class MessageBrokerConfig implements WebSocketMessageBrokerConfigurer {

    private final StompHandler stompHandler;
    private final ObjectMapper objectMapper;

    @Value("${client.prod.front-url}")
    private String prodFrontUrl;

    @Value("${client.dev.front-url}")
    private String devFrontUrl;

    public MessageBrokerConfig(StompHandler stompHandler, ObjectMapper objectMapper) {
        this.stompHandler = stompHandler;
        this.objectMapper = objectMapper;
    }

    @Bean
    public ThreadPoolTaskScheduler customMessageBrokerTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("wss-heartbeat-thread-");
        scheduler.initialize();
        return scheduler;
    }

    /**
     * 토큰 추출을 위한 핸드셰이크 인터셉터
     * URL 파라미터에서 토큰을 추출하여 WebSocket 세션 속성에 저장
     */
    @Bean
    public HandshakeInterceptor tokenHandshakeInterceptor() {
        return new HandshakeInterceptor() {
            @Override
            public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                           WebSocketHandler wsHandler, Map<String, Object> attributes) {
                // URL 파라미터에서 token을 추출하여 세션 속성에 저장
                if (request instanceof ServletServerHttpRequest) {
                    ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
                    String token = servletRequest.getServletRequest().getParameter("token");
                    if (token != null) {
                        attributes.put("token", token);
                    }
                }
                return true;
            }

            @Override
            public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                       WebSocketHandler wsHandler, Exception exception) {
                // 연결 성공 로깅
                if (exception == null) {
                    if (request instanceof ServletServerHttpRequest) {
                        ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
                        String remoteAddr = servletRequest.getServletRequest().getRemoteAddr();
                        log.info("WebSocket 연결 성공: {}", remoteAddr);

                        // 토큰 여부 확인 로깅
                        String token = servletRequest.getServletRequest().getParameter("token");
                        if (token != null) {
                            log.info("토큰 파라미터가 포함된 WebSocket 연결 성공");
                        }
                    }
                } else {
                    // 연결 실패 시 로깅
                    log.error("WebSocket 핸드셰이크 중 오류 발생", exception);
                }
            }
        };
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {

        // TODO: 프로덕션 환경에서는 특정 오리진만 허용하도록 수정
        // @Value로 주입받은 prodFrontUrl, devFrontUrl

        // SockJS를 사용하는 WebSocket 엔드포인트
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")  // 개발 환경에서는 모든 오리진 허용
                .addInterceptors(tokenHandshakeInterceptor())  // 재사용 가능한 인터셉터 적용
                .withSockJS()
                .setDisconnectDelay(30 * 1000)  // 연결 해제 후 세션 유지 시간 (30초)
                .setHeartbeatTime(25 * 1000)    // 하트비트 주기 (25초)
                .setClientLibraryUrl("https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js");

        // 순수 WebSocket 엔드포인트 (SockJS 없이 직접 연결)
        registry.addEndpoint("/ws/raw")
                .setAllowedOriginPatterns("*")  // 개발 환경에서는 모든 오리진 허용
                .addInterceptors(tokenHandshakeInterceptor());  // 동일한 인터셉터 재사용
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 클라이언트가 메시지를 발행할 수 있는 목적지
        registry.setApplicationDestinationPrefixes("/pub");

        // 메시지 브로커가 구독 요청을 처리할 목적지 접두사
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
     * WebSocket 메시지에 대한 사용자 정의 메시지 변환기를 구성합니다.
     * Jackson을 설정하여 JSR310(Java 8 날짜/시간 타입 - LocalDateTime 등)을 처리할 수 있도록 합니다.
     */
    @Override
    public boolean configureMessageConverters(List<MessageConverter> messageConverters) {
        DefaultContentTypeResolver resolver = new DefaultContentTypeResolver();
        resolver.setDefaultMimeType(MimeTypeUtils.APPLICATION_JSON);

        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(objectMapper);
        converter.setContentTypeResolver(resolver);
        messageConverters.add(converter);

        return false;
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