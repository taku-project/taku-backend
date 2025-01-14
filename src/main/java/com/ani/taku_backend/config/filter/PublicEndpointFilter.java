package com.ani.taku_backend.config.filter;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.ani.taku_backend.auth.util.JwtUtil;
import com.ani.taku_backend.common.exception.ErrorCode;
import com.ani.taku_backend.common.response.CommonResponse;
import com.ani.taku_backend.user.model.dto.PrincipalUser;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


/**
 * 공개 엔드포인트 필터
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PublicEndpointFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        log.info("PublicEndpointFilter");

        // SecurityContext에서 인증 객체 확인
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // TODO: jwt 인증 완료된 요청 처리 공통 함수 처리
        if (authentication != null && authentication.isAuthenticated()) {
            log.info("JWT 인증 완료된 요청: {}", authentication.getName());
        } else {

            String authorizationHeader = request.getHeader("Authorization");
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                String accessToken = authorizationHeader.substring(7);

                try {
                    PrincipalUser principalUser = new PrincipalUser(jwtUtil.getUserFromToken(accessToken));
                    log.info("principalUser : {}", principalUser);
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
                log.info("anonymous user");
            }
        }
        
        filterChain.doFilter(request, response);
    }
        // 유효하지 않은 토큰 처리
    private void handleInvalidToken(HttpServletResponse response, Exception e) throws IOException {
        log.error("유효하지 않은 토큰", e);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json;charset=UTF-8");

        CommonResponse<Void> errorResponse = CommonResponse.fail(ErrorCode.INVALID_TOKEN);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
