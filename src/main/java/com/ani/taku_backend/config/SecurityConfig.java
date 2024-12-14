package com.ani.taku_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.web.SecurityFilterChain;

import com.ani.taku_backend.auth.service.OAuth2UserService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;


@Configuration
@EnableMethodSecurity
@EnableWebSecurity
@RequiredArgsConstructor
@Log4j2
public class SecurityConfig {

    private final OAuth2UserService oAuth2UserService;
    
    @Value("${client.web-url}")
    private String webUrl;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers
                .frameOptions(frame -> frame.disable())
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/",
                    "/login",
                    "/oauth2/authorization/**",
                    "/login/oauth2/code/**",
                    "/api/auth/**"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(oAuth2UserService)
                )
                .successHandler((request, response, authentication) -> {
                    log.info("OAuth2 인증 최종 성공 - User: {}", 
                        authentication.getName());

                        // 토큰 만들기

                        // URL 만들기 + 토큰 넣어서

                        // 리다이렉트 하기
                })
                .failureHandler((request, response, exception) -> {
                    if (exception instanceof OAuth2AuthenticationException) {
                        OAuth2Error error = ((OAuth2AuthenticationException) exception).getError();
                        
                        // TODO : 어떠한 에러인지 확인 후 처리 또는 로깅 필요
                        if (!"invalid_request".equals(error.getErrorCode())) {
                            log.error("OAuth2 인증 실패 - Error: {}, Description: {}", 
                                error.getErrorCode(), error.getDescription());
                        }

                        if (error.getErrorCode().equals("NOT_FOUND_USER") && error.getUri() != null) {
                            response.sendRedirect(error.getUri());
                            return;
                        }
                    }
                    
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, 
                        "인증 실패: " + exception.getMessage());
                })
            );

        return http.build();
    }
  
}
