package com.ani.taku_backend.chatroom;

import com.ani.taku_backend.chatroom.repository.ChatRoomRepository;
import com.ani.taku_backend.chatroom.model.entity.ChatRoom;
import com.ani.taku_backend.chatroom.service.ChatAuthorizationService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StompHandler implements ChannelInterceptor {

    @Value("${jwt.secret}")
    private String secretKey;
    
    private final ChatAuthorizationService chatAuthorizationService;
    private final ChatRoomRepository chatRoomRepository;
    
    public StompHandler(ChatAuthorizationService chatAuthorizationService, ChatRoomRepository chatRoomRepository) {
        this.chatAuthorizationService = chatAuthorizationService;
        this.chatRoomRepository = chatRoomRepository;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        final StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        
        try {
            StompCommand command = accessor.getCommand();
            
            if (StompCommand.CONNECT == command) {
                handleConnectCommand(accessor);
            } else if (StompCommand.SUBSCRIBE == command) {
                handleSubscribeCommand(accessor);
            } else if (StompCommand.DISCONNECT == command) {
                handleDisconnectCommand(accessor);
            }
        } catch (ExpiredJwtException e) {
            log.error("토큰이 만료되었습니다", e);
            throw new AuthenticationServiceException("토큰이 만료되었습니다");
        } catch (MalformedJwtException | SignatureException e) {
            log.error("유효하지 않은 토큰입니다", e);
            throw new AuthenticationServiceException("유효하지 않은 토큰입니다");
        } catch (Exception e) {
            log.error("WebSocket 인증 처리 중 오류 발생", e);
            throw new AuthenticationServiceException("WebSocket 인증 처리 중 오류: " + e.getMessage());
        }

        return message;
    }
    
    /**
     * CONNECT 명령 처리: 연결 시 토큰 검증
     */
    private void handleConnectCommand(StompHeaderAccessor accessor) {
        log.info("STOMP CONNECT 요청 처리 - 토큰 검증 시작");
        
        String token = extractToken(accessor);
        Claims claims = validateToken(token);
        
        log.info("WebSocket 연결 토큰 검증 완료 - 사용자: {}", claims.getSubject());
    }
    
    /**
     * SUBSCRIBE 명령 처리: 채팅방 구독 권한 확인
     */
    private void handleSubscribeCommand(StompHeaderAccessor accessor) {
        log.info("STOMP SUBSCRIBE 요청 처리 - 채팅방 구독 권한 확인");
        
        String destination = accessor.getDestination();
        if (destination == null || !destination.startsWith("/sub/chat/room/")) {
            log.warn("구독 대상이 올바르지 않습니다: {}", destination);
            return;
        }
        
        String token = extractToken(accessor);
        Claims claims = validateToken(token);
        String email = claims.getSubject();
        
        // /sub/chat/room/{wsRoomId} 형식에서 wsRoomId 추출
        String wsRoomId = destination.split("/")[4];
        log.info("채팅방 구독 요청 - 사용자: {}, 채팅방 WS ID: {}", email, wsRoomId);
        
        // wsRoomId를 통해 실제 채팅방 ID 조회
        ChatRoom chatRoom = findChatRoomByWsId(wsRoomId, email);
        
        // 해당 채팅방 참여 권한 확인
        if (!chatAuthorizationService.isRoomParticipant(email, chatRoom.getId())) {
            log.error("사용자 {}는 채팅방 {}에 접근 권한이 없습니다", email, chatRoom.getId());
            throw new AuthenticationServiceException("해당 채팅방에 접근 권한이 없습니다.");
        }
        
        log.info("채팅방 구독 권한 확인 완료 - 사용자: {}, 채팅방: {}", email, chatRoom.getId());
    }
    
    /**
     * DISCONNECT 명령 처리
     */
    private void handleDisconnectCommand(StompHeaderAccessor accessor) {
        log.info("STOMP DISCONNECT 요청 처리 - 세션 ID: {}", accessor.getSessionId());
    }
    
    /**
     * 채팅방 wsRoomId로 ChatRoom 엔티티 조회
     */
    private ChatRoom findChatRoomByWsId(String wsRoomId, String email) {
        return chatRoomRepository.findByWsRoomId(wsRoomId)
                .orElseThrow(() -> {
                    log.error("사용자 {}의 구독 요청 처리 중 채팅방을 찾을 수 없습니다: {}", email, wsRoomId);
                    return new AuthenticationServiceException("채팅방을 찾을 수 없습니다");
                });
    }
    
    /**
     * StompHeaderAccessor에서 토큰 추출
     */
    private String extractToken(StompHeaderAccessor accessor) {
        // 1. 헤더에서 찾기
        String bearerToken = accessor.getFirstNativeHeader("Authorization");

        // 2. 헤더에 없으면 세션 속성에서 찾기
        if (bearerToken == null) {
            Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
            if (sessionAttributes != null && sessionAttributes.containsKey("token")) {
                Object tokenObj = sessionAttributes.get("token");
                if (tokenObj instanceof String) {
                    bearerToken = "Bearer " + tokenObj;
                }
            }
        }

        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            log.error("토큰 형식이 올바르지 않습니다: {}", bearerToken);
            throw new AuthenticationServiceException("토큰 형식이 올바르지 않습니다.");
        }

        return bearerToken.substring(7);
    }

    /**
     * JWT 토큰을 검증하고 클레임을 반환합니다.
     * 
     * @param token 검증할 JWT 토큰
     * @return 검증된 Claims 객체
     */
    private Claims validateToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
