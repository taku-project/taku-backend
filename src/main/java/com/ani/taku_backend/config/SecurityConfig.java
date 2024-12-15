package com.ani.taku_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.ani.taku_backend.auth.handler.OAuth2AuthenticationHandler;
import com.ani.taku_backend.auth.service.OAuth2UserService;
import com.ani.taku_backend.config.filter.JwtAuthenticationFilter;
import com.ani.taku_backend.config.filter.RefreshTokenFilter;

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
    private final OAuth2AuthenticationHandler.OAuth2SuccessHandler oAuth2SuccessHandler;
    private final OAuth2AuthenticationHandler.OAuth2FailureHandler oAuth2FailureHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RefreshTokenFilter refreshTokenFilter;
    
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
            // TODO : 개발 과정에서 현재 모든 요청을 허용하고 있음. 추후 권한 관리 필요
                .requestMatchers(
                    "/",
                    "/login",
                    "/oauth2/authorization/**",
                    "/login/oauth2/code/**",
                    "/api/user/**",
                    "/api/user/register",
                    "/h2-console/**",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/swagger-ui.html",
                    "/swagger-resources/**",
                    "/webjars/**"
                ).permitAll()
                .anyRequest().authenticated()
                // .anyRequest().permitAll()
            )
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(oAuth2UserService)
                )
                .successHandler(this.oAuth2SuccessHandler)
                .failureHandler(this.oAuth2FailureHandler)
            )
            .addFilterBefore(refreshTokenFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthenticationFilter, RefreshTokenFilter.class);

        return http.build();
    }
  
}
