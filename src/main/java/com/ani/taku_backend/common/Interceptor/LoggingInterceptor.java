package com.ani.taku_backend.common.Interceptor;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class LoggingInterceptor implements HandlerInterceptor {

    private long startTime;
    @Value("${jwt.secret}")
    private String secretKey;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler){

        startTime = System.currentTimeMillis();

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,Exception ex){

        String memberId = String.valueOf(extractMemberIdFromJwt(request));

        if(memberId.equals("null")){
            memberId="GUEST";
        }
        String requestUri = request.getRequestURI();
        String requestParams = request.getQueryString();
        int status = response.getStatus();
        long duration = System.currentTimeMillis() - startTime;

        if (requestParams != null) {
            requestParams = URLDecoder.decode(requestParams, StandardCharsets.UTF_8);
        }

        Instant now = Instant.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.of("Asia/Seoul"));
        String formattedTime = formatter.format(now);

        log.info("Time: {}, clientId: {}, URI: {}, Params: {}, Response: {}, Duration: {}", formattedTime, memberId, requestUri,requestParams,  status,  duration);


    }
    // JWT에서 memberId를 추출하는 메서드
    private Long extractMemberIdFromJwt(HttpServletRequest request) {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring("Bearer ".length()).trim(); // "Bearer " 제거
            try {
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
                        .build()
                        .parseClaimsJws(token)
                        .getBody();


                return claims.get("userId", Long.class); // memberId 추출
            } catch (Exception e) {
                log.error("Invalid JWT token", e);
            }
        }
        return null; // 토큰이 없거나 유효하지 않으면 null 반환
    }
}
