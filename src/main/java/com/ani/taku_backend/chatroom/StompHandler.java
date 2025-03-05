package com.ani.taku_backend.chatroom;

import com.ani.taku_backend.chatroom.service.ChatService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Component;

@Lazy
@Component
@Slf4j
public class StompHandler implements ChannelInterceptor {

    @Value("${jwt.secret}")
    private String secretKey;
    private final ChatService chatService;

    public StompHandler(ChatService chatService) {
        this.chatService = chatService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        final StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        
        try {
            // CONNECT 요청 처리: 연결 시 토큰 검증
            if (StompCommand.CONNECT == accessor.getCommand()) {
                log.info("STOMP CONNECT 요청 처리 - 토큰 검증 시작");
                
                String bearerToken = accessor.getFirstNativeHeader("Authorization");
                if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
                    log.error("토큰 형식이 올바르지 않습니다: {}", bearerToken);
                    throw new AuthenticationServiceException("토큰 형식이 올바르지 않습니다.");
                }
                
                String token = bearerToken.substring(7);
                // 토큰 검증
                Claims claims = validateToken(token);
                log.info("WebSocket 연결 토큰 검증 완료 - 사용자: {}", claims.getSubject());
            }
            
            // SUBSCRIBE 요청 처리: 채팅방 구독 권한 확인
            else if (StompCommand.SUBSCRIBE == accessor.getCommand()) {
                log.info("STOMP SUBSCRIBE 요청 처리 - 채팅방 구독 권한 확인");
                
                String destination = accessor.getDestination();
                if (destination == null || !destination.startsWith("/sub/chat/room/")) {
                    log.warn("구독 대상이 올바르지 않습니다: {}", destination);
                    return message;
                }
                
                String bearerToken = accessor.getFirstNativeHeader("Authorization");
                if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
                    log.error("토큰 형식이 올바르지 않습니다: {}", bearerToken);
                    throw new AuthenticationServiceException("토큰 형식이 올바르지 않습니다.");
                }
                
                String token = bearerToken.substring(7);
                Claims claims = validateToken(token);
                String email = claims.getSubject();
                
                // /sub/chat/room/{roomId} 형식에서 roomId 추출
                String roomId = destination.split("/")[4];
                log.info("채팅방 구독 요청 - 사용자: {}, 채팅방: {}", email, roomId);
                
                // 해당 채팅방 참여 권한 확인
                if (!chatService.isRoomParticipant(email, Long.parseLong(roomId))) {
                    log.error("사용자 {}는 채팅방 {}에 접근 권한이 없습니다", email, roomId);
                    throw new AuthenticationServiceException("해당 채팅방에 접근 권한이 없습니다.");
                }
                
                log.info("채팅방 구독 권한 확인 완료 - 사용자: {}, 채팅방: {}", email, roomId);
            }
            
            // DISCONNECT 요청 처리
            else if (StompCommand.DISCONNECT == accessor.getCommand()) {
                log.info("STOMP DISCONNECT 요청 처리 - 세션 ID: {}", accessor.getSessionId());
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
     * JWT 토큰을 검증하고 클레임을 반환합니다.
     * 
     * @param token 검증할 JWT 토큰
     * @return 검증된 Claims 객체
     */
    private Claims validateToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
