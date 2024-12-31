package com.ani.taku_backend.auth.handler;

import com.ani.taku_backend.auth.util.JwtUtil;
import com.ani.taku_backend.common.service.RedisService;
import com.ani.taku_backend.user.model.entity.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;

@Component
@RequiredArgsConstructor
@Log4j2
public class OAuth2AuthenticationHandler {

    private final JwtUtil jwtUtil;
    private final RedisService redisService;

    @Value("${jwt.access-token-validity}")
    private Long accessTokenValidityTime;

    @Value("${jwt.refresh-token-validity}")
    private Long refreshTokenValidityTime;

    @Value("${client.login-success-url}")
    private String loginSuccessUrl;

    @Component
    public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {
        @Override
        public void onAuthenticationSuccess(
            HttpServletRequest request, 
            HttpServletResponse response, 
            Authentication authentication
        ) throws IOException {
            log.info("OAuth2 인증 최종 성공 - User: {}", authentication.getName());

            OAuth2User principal = (OAuth2User) authentication.getPrincipal();
            log.info("authentication.getAuthorities() : {}", authentication.getAuthorities());

            User user = (User) principal.getAttributes().get("user");

            // 토큰 만들기
            String accessToken = jwtUtil.createAccessToken(user);

            // redis에 access token 저장
            redisService.setKeyValue("accessToken:%s".formatted(user.getEmail()), accessToken, Duration.ofMillis(accessTokenValidityTime));

            // refresh token 만들기
            String refreshToken = jwtUtil.createRefreshToken(user);

            // redis에 refresh token 저장
            redisService.setKeyValue("refreshToken:%s".formatted(user.getEmail()), refreshToken, Duration.ofMillis(refreshTokenValidityTime));

            // 쿠키에 refresh token 저장
            setRefreshTokenCookie(response, refreshToken);

            // 응답 헤더에 access token 추가
            response.setHeader("Authorization", "Bearer " + accessToken);
            log.info("accessToken : {}", accessToken);

            // URL 만들기 + 토큰 넣어서
            String redirectUrl = UriComponentsBuilder
                .fromUriString(loginSuccessUrl)
                .build()
                .toUriString();

            log.info("redirectUrl : {}", redirectUrl);

            // 리다이렉트 하기
            response.sendRedirect(redirectUrl);
        }

        /**
         * 쿠키에 refresh token 저장
         * @param response
         * @param refreshToken
         */
        public void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
            Cookie refreshTokenCookie = new Cookie("Refresh-Token", refreshToken);
            refreshTokenCookie.setHttpOnly(true); // 클라이언트에서 직접 접근 불가

            //TODO: 추후 HTTPS 적용 시 활성화
            // refreshTokenCookie.setSecure(true); // HTTPS에서만 전송

            refreshTokenCookie.setPath("/"); // 모든 경로에서 쿠키 접근 가능
            refreshTokenCookie.setMaxAge((int) Duration.ofDays(1).toSeconds()); // 유효기간 설정 (초 단위)
            response.addCookie(refreshTokenCookie);
        }
    }

    @Component
    public class OAuth2FailureHandler implements AuthenticationFailureHandler {
        @Override
        public void onAuthenticationFailure(
            HttpServletRequest request, 
            HttpServletResponse response, 
            AuthenticationException exception
        ) throws IOException {
            if (exception instanceof OAuth2AuthenticationException) {
                OAuth2Error error = ((OAuth2AuthenticationException) exception).getError();

                log.error("OAuth2 인증 실패 - Error: {}, Description: {}", 
                    error.getErrorCode(), error.getDescription());

                if (!"invalid_request".equals(error.getErrorCode())) {
                    log.error("OAuth2 인증 실패 - Error: {}, Description: {}", 
                        error.getErrorCode(), error.getDescription());
                }

                if (error.getErrorCode().equals("NOT_FOUND_USER") && error.getUri() != null) {
                    response.setHeader("Authorization", "Bearer " + error.getDescription());
                    response.sendRedirect(error.getUri());
                    return;
                }
            }
            
            response.sendError(
                HttpServletResponse.SC_UNAUTHORIZED, 
                "인증 실패: " + exception.getMessage()
            );
        }
    }
}
