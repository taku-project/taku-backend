package com.ani.taku_backend.config.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.ani.taku_backend.auth.util.JwtUtil;
import com.ani.taku_backend.common.exception.ErrorCode;
import com.ani.taku_backend.common.response.ApiResponse;
import com.ani.taku_backend.config.SecurityPathConfig;
import com.ani.taku_backend.user.model.dto.PrincipalUser;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.ExpiredJwtException;

import java.io.IOException;

/**
 * JWT 토큰 인증 필터
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authorizationHeader = request.getHeader("Authorization");
        
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String accessToken = authorizationHeader.substring(7);

            try {
                PrincipalUser principalUser = new PrincipalUser(jwtUtil.getUserFromToken(accessToken));
                response.setHeader("Authorization", "Bearer " + accessToken);
                SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(principalUser, null, null)
                );

            } catch (ExpiredJwtException e) {
                // 토큰이 만료되었을 때 요청 속성에 더 자세한 정보를 담아서 전달
                request.setAttribute("expired", true);
                request.setAttribute("expiredToken", accessToken);  // 만료된 토큰 정보
                request.setAttribute("expiredTokenClaims", e.getClaims());  // 만료된 토큰의 클레임 정보
            } catch (Exception e) {
                handleInvalidToken(response, e);
                return;
            }
        }else{
            handleNotFoundToken(response, null);
            return;
        }
        filterChain.doFilter(request, response);
    }
    
    // 유효하지 않은 토큰 처리
    private void handleInvalidToken(HttpServletResponse response, Exception e) throws IOException {
        log.error("유효하지 않은 토큰", e);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json;charset=UTF-8");

        ApiResponse<Void> errorResponse = ApiResponse.fail(ErrorCode.INVALID_TOKEN);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

    // 토큰을 헤더에서 발견하지 못했을때
    private void handleNotFoundToken(HttpServletResponse response, Exception e) throws IOException {
        log.error("토큰이 없습니다.", e);
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/json;charset=UTF-8");
        
        ApiResponse<Void> errorResponse = ApiResponse.fail(ErrorCode.EMPTY_TOKEN);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

    // 필터 스킵 여부 결정
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return SecurityPathConfig.shouldSkipFilter(
            request.getRequestURI(),
            request.getMethod()
        );
    }
}