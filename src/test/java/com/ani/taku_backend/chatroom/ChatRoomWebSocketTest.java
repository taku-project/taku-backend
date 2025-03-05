package com.ani.taku_backend.chatroom;

import com.ani.taku_backend.chatroom.model.constant.ParticipantRole;
import com.ani.taku_backend.chatroom.model.document.ChatMessage;
import com.ani.taku_backend.chatroom.model.document.ChatRoomMetaInfo;
import com.ani.taku_backend.chatroom.model.dto.ChatMessageRequestDTO;
import com.ani.taku_backend.chatroom.model.dto.ChatReadStatusDTO;
import com.ani.taku_backend.chatroom.model.entity.ChatRoom;
import com.ani.taku_backend.chatroom.repository.ChatRoomMetaRepository;
import com.ani.taku_backend.chatroom.repository.ChatRoomRepository;
import com.ani.taku_backend.common.enums.ProviderType;
import com.ani.taku_backend.common.enums.UserRole;
import com.ani.taku_backend.user.model.entity.User;
import com.ani.taku_backend.user.model.entity.UserStatus;
import com.ani.taku_backend.user.repository.UserRepository;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ChatRoomWebSocketTest {

    @LocalServerPort
    private int port;

    private BlockingQueue<ChatMessage> receivedMessages;
    private BlockingQueue<ChatReadStatusDTO> receivedReadStatuses;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private ChatRoomMetaRepository  chatRoomMetaRepository;

    @Value("${jwt.secret}")
    private String secretKeyBase64; // JWT 시크릿 키(base64 인코딩)

    private User testUser;
    private ChatRoom testRoom;
    private String testToken;
    private String rawToken; // Authorization 헤더에서 "Bearer " 제외한 토큰


    @BeforeEach
    public void setUp() {
        receivedMessages = new LinkedBlockingDeque<>();
        receivedReadStatuses = new LinkedBlockingDeque<>();

        setupTestData();

        rawToken = generateTestJwtToken();
        testToken = "Bearer " + rawToken;

        ChatRoomMetaInfo metaInfo = new ChatRoomMetaInfo(testRoom.getId());
        metaInfo.getParticipants().addParticipant(testUser.getUserId(), ParticipantRole.BUYER);
        chatRoomMetaRepository.save(metaInfo);
    }


    /**
     * 테스트용 JWT 액세스 토큰 생성 메서드
     * JwtUtil의 createAccessToken() 메서드와 유사한 형태로 구현
     */
    private String generateTestJwtToken() {
        Map<String, Object> claims = new HashMap<>();

        claims.put("userId", testUser.getUserId());
        claims.put("email", testUser.getEmail());
        claims.put("nickname", testUser.getNickname());
        claims.put("role", "ROLE_USER");
        claims.put("providerType", ProviderType.KAKAO.name());
        claims.put("profileImg", testUser.getProfileImg());
        claims.put("status", UserStatus.ACTIVE.name());
        claims.put("domesticId", testUser.getDomesticId());
        claims.put("gender", testUser.getGender());
        claims.put("ageRange", testUser.getAgeRange());
        claims.put("type", "ACCESS");

        Date now = new Date();
        Date validity = new Date(now.getTime() + 3600000); // 1시간


        return Jwts.builder()
                .setClaims(claims)
                .setSubject(testUser.getEmail())
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(Keys.hmacShaKeyFor(secretKeyBase64.getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }

    private void setupTestData() {
        // 테스트 사용자 생성
        testUser = userRepository.findByEmail("test@example.com")
                .orElseGet(() -> userRepository.save(User.builder()
                        .email("test@example.com")
                        .nickname("테스트유저")
                        .role(UserRole.USER)
                        .status(UserStatus.ACTIVE)
                        .providerType(ProviderType.KAKAO.name())
                        .build()));

        // 테스트 채팅방 생성
        testRoom = chatRoomRepository.save(ChatRoom.testBuilder()
                .articleId(1L)
                .wsRoomId("test-room-id")
                .build());
    }

    /**
     * 인증 토큰을 처리하는 세션 핸들러
     */
    private class AuthTokenStompSessionHandler extends StompSessionHandlerAdapter {
        @Override
        public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
            System.out.println("STOMP 연결 성공: " + session.getSessionId());
            super.afterConnected(session, connectedHeaders);
        }

        @Override
        public void handleException(StompSession session, StompCommand command,
                                    StompHeaders headers, byte[] payload, Throwable exception) {
            System.err.println("STOMP 예외 발생: " + exception.getMessage());
            exception.printStackTrace();
            super.handleException(session, command, headers, payload, exception);
        }

        @Override
        public void handleTransportError(StompSession session, Throwable exception) {
            System.err.println("STOMP 전송 오류: " + exception.getMessage());
            exception.printStackTrace();
            super.handleTransportError(session, exception);
        }
    }

    @DisplayName("채팅방에 메시지를 전송하면 해당 메시지가 구독자에게 전달된다")
    @Test
    public void sendMessageTest() throws Exception {
        // 웹소켓 스톰프 클라이언트 설정
        WebSocketStompClient stompClient = createWebSocketStompClient();

        // 웹소켓 HTTP 헤더 설정
        WebSocketHttpHeaders httpHeaders = new WebSocketHttpHeaders();
        httpHeaders.add("Authorization", testToken);

        // STOMP 헤더 설정
        StompHeaders stompHeaders = new StompHeaders();
        stompHeaders.add("Authorization", testToken);

        // 세션 핸들러 생성
        StompSessionHandler sessionHandler = new AuthTokenStompSessionHandler();

        // WebSocket 연결 (HTTP 헤더와 STOMP 헤더 모두 설정)
        StompSession session = stompClient
                .connect("ws://localhost:" + port + "/ws", httpHeaders, stompHeaders, sessionHandler)
                .get(5, TimeUnit.SECONDS);

        // 채팅방 구독 (Authorization 헤더 추가)
        StompHeaders subscribeHeaders = new StompHeaders();
        subscribeHeaders.add("Authorization", testToken);
        subscribeHeaders.setDestination("/sub/chat/room/" + testRoom.getWsRoomId());
        session.subscribe(subscribeHeaders, new ChatMessageStompFrameHandler());

        //테스트 메세지 생성.
        String messageContent = "안녕하세요! 테스트 메시지입니다.";
        ChatMessageRequestDTO messageRequest = new ChatMessageRequestDTO(
                testRoom.getWsRoomId(),
                testUser.getUserId(),
                messageContent
        );

        // 메시지 전송 (Authorization 헤더 추가)
        StompHeaders sendHeaders = new StompHeaders();
        sendHeaders.add("Authorization", testToken);
        sendHeaders.setDestination("/pub/chat/message");
        session.send(sendHeaders, messageRequest);

        // 메시지 수신 확인 (5초 타임아웃)
        ChatMessage receivedMessage = receivedMessages.poll(5, TimeUnit.SECONDS);

        // 검증
        assertThat(receivedMessage).isNotNull();
        assertThat(receivedMessage.getContent()).isEqualTo(messageContent);
        assertThat(receivedMessage.getSenderId()).isEqualTo(testUser.getUserId());
        assertThat(receivedMessage.getChatRoomId()).isEqualTo(testRoom.getId());

        // 세션 연결 해제
        session.disconnect();
    }

    @DisplayName("채팅방 메시지를 읽음 처리하면 구독자에게 읽음 상태가 전달된다")
    @Test
    public void markMessageAsReadTest() throws Exception {
        // 웹소켓 스톰프 클라이언트 설정
        WebSocketStompClient stompClient = createWebSocketStompClient();

        // 웹소켓 HTTP 헤더 설정
        WebSocketHttpHeaders httpHeaders = new WebSocketHttpHeaders();
        httpHeaders.add("Authorization", testToken);

        // STOMP 헤더 설정
        StompHeaders stompHeaders = new StompHeaders();
        stompHeaders.add("Authorization", testToken);

        // 세션 핸들러 생성
        StompSessionHandler sessionHandler = new AuthTokenStompSessionHandler();

        // WebSocket 연결 (HTTP 헤더와 STOMP 헤더 모두 설정)
        StompSession session = stompClient
                .connect("ws://localhost:" + port + "/ws", httpHeaders, stompHeaders, sessionHandler)
                .get(5, TimeUnit.SECONDS);

        // 채팅방 읽음 상태 구독
        StompHeaders subscribeHeaders = new StompHeaders();
        subscribeHeaders.add("Authorization", testToken);
        subscribeHeaders.setDestination("/sub/chat/room/" + testRoom.getWsRoomId() + "/read");
        session.subscribe(subscribeHeaders, new ReadStatusStompFrameHandler());

        // 읽음 처리 메시지 전송
        StompHeaders sendHeaders = new StompHeaders();
        sendHeaders.add("Authorization", testToken);
        sendHeaders.setDestination("/pub/chat/read");

        // 읽음 처리 DTO
        session.send(sendHeaders,
                ChatMessageRequestDTO.forReadStatus(testRoom.getWsRoomId(), testUser.getUserId()));

        // 읽음 상태 메시지 수신 확인 (5초 타임아웃)
        ChatReadStatusDTO readStatus = receivedReadStatuses.poll(5, TimeUnit.SECONDS);

        // 읽음 상태 메시지 검증
        assertThat(readStatus).isNotNull();
        assertThat(readStatus.chatRoomId()).isEqualTo(testRoom.getId());
        assertThat(readStatus.senderId()).isEqualTo(testUser.getUserId());

        // 세션 연결 해제
        session.disconnect();
    }

    private WebSocketStompClient createWebSocketStompClient() {
        StandardWebSocketClient standardWebSocketClient = new StandardWebSocketClient();
        WebSocketTransport webSocketTransport = new WebSocketTransport(standardWebSocketClient);
        List<Transport> transports = Collections.singletonList(webSocketTransport);
        SockJsClient sockJsClient = new SockJsClient(transports);

        WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);
        

        MappingJackson2MessageConverter messageConverter = new MappingJackson2MessageConverter();
        messageConverter.getObjectMapper().registerModule(new JavaTimeModule());
        stompClient.setMessageConverter(messageConverter);
        
        return stompClient;
    }

    // ChatMessage용 STOMP 프레임 핸들러
    private class ChatMessageStompFrameHandler implements StompFrameHandler {
        @Override
        public Type getPayloadType(StompHeaders headers) {
            return ChatMessage.class;
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            System.out.println("메시지 수신: " + payload);
            receivedMessages.offer((ChatMessage) payload);
        }
    }
    
    // ReadStatus용 STOMP 프레임 핸들러
    private class ReadStatusStompFrameHandler implements StompFrameHandler {
        @Override
        public Type getPayloadType(StompHeaders headers) {
            return ChatReadStatusDTO.class;
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            System.out.println("읽음 상태 수신: " + payload);
            receivedReadStatuses.offer((ChatReadStatusDTO) payload);
        }
    }
}