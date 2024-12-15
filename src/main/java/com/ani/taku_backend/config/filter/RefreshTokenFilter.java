package com.ani.taku_backend.config.filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.ani.taku_backend.auth.util.JwtUtil;
import com.ani.taku_backend.common.exception.UserException.UserNotFoundException;
import com.ani.taku_backend.user.model.entity.User;

import io.jsonwebtoken.Claims;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
@Log4j2
public class RefreshTokenFilter extends OncePerRequestFilter {

    private static final String REFRESH_TOKEN_COOKIE_NAME = "Refresh-Token";
    private static final String REFRESH_TOKEN_REDIS_PREFIX = "refreshToken:";

    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;
    private final com.ani.taku_backend.user.repository.UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // 만료된 토큰이 아니라면 필터 체인을 넘김
        if (!Boolean.TRUE.equals(request.getAttribute("expired"))) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String refreshToken = extractRefreshToken(request);
            if (refreshToken == null) {
                // 리프레시 토큰이 없다면 인증 실패
                handleUnauthorized(response, "Refresh Token not found");
                return;
            }

            processRefreshToken(refreshToken, response);
            
        } catch (Exception e) {
            // 리프레시 토큰 검증 실패
            handleUnauthorized(response, "Refresh Token verification failed");
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 리프레시 토큰 추출
     * @param request
     * @return
     */
    private String extractRefreshToken(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        
        return Arrays.stream(request.getCookies())
                .filter(cookie -> REFRESH_TOKEN_COOKIE_NAME.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    /**
     * 리프레시 토큰 검증 및 새로운 엑세스 토큰 생성
     * @param refreshToken
     * @param response
     * @throws Exception
     */
    private void processRefreshToken(String refreshToken, HttpServletResponse response) throws Exception {
        Claims refreshTokenClaims = jwtUtil.extractAllClaims(refreshToken);
        String email = refreshTokenClaims.get("email").toString();
        
        String savedToken = redisTemplate.opsForValue().get(REFRESH_TOKEN_REDIS_PREFIX + email);
        
        if (!refreshToken.equals(savedToken)) {
            handleUnauthorized(response, "Invalid Refresh Token");
            return;
        }

        User user = getUserFromClaims(refreshTokenClaims);
        String newAccessToken = jwtUtil.createAccessToken(user);
        
        updateSecurityContextAndResponse(user, newAccessToken, response);
    }

    /**
     * 톸큰 클레임에서 유저 정보 추출
     * @param claims
     * @return
     */
    private User getUserFromClaims(Claims claims) {
        return userRepository.findById(Long.parseLong(claims.get("sub").toString()))
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    /**
     * 새로운 엑세스 토큰 생성 및 응답 헤더 업데이트
     * @param user
     * @param newAccessToken
     * @param response
     */
    private void updateSecurityContextAndResponse(User user, String newAccessToken, HttpServletResponse response) {
        log.info("New Access Token generated: {}", newAccessToken);
        response.setHeader("Authorization", "Bearer " + newAccessToken);
        
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(user, null, null)
        );
    }

    /**
     * 에러 처리
     * @param response
     * @param message
     * @throws IOException
     */
    private void handleUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(message);
    }
}
