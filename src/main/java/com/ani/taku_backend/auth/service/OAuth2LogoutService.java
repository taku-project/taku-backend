package com.ani.taku_backend.auth.service;

import com.ani.taku_backend.auth.util.JwtUtil;
import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.exception.JwtException;
import com.ani.taku_backend.common.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import static com.ani.taku_backend.common.exception.ErrorCode.EMPTY_TOKEN;
import static com.ani.taku_backend.common.exception.ErrorCode.INVALID_TOKEN;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2LogoutService {

    private final RedisService redisService;
    private final JwtUtil jwtUtil;

    // 구글 반영해야함
    public String logout(String accessTokenHeader) {
        String extractedToken = extractBearerToken(accessTokenHeader);
        log.info("Extracted Token: {}", extractedToken);

        if (extractedToken == null || extractedToken.isEmpty()) {
            throw new DuckwhoException(EMPTY_TOKEN);
        }

        try {
            String email = jwtUtil.getEmailFromToken(extractedToken);
            redisService.deleteKeyValue("accessToken:" + email);
            redisService.deleteKeyValue("refreshToken:" + email);
            return "로그아웃 성공";
        } catch (Exception e) {
            log.error("로그아웃 중 오류 발생", e);
            throw new DuckwhoException(INVALID_TOKEN);
        }
    }


    private String extractBearerToken(String accessTokenHeader) {
        if (accessTokenHeader != null && accessTokenHeader.startsWith("Bearer ")) {
            return accessTokenHeader.substring(7);
        }
        return null;
    }
}
