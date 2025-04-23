package com.ani.taku_backend.config;

import com.ani.taku_backend.common.enums.UserRole;
import org.apache.http.protocol.HTTP;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.ani.taku_backend.auth.handler.OAuth2AuthenticationHandler;
import com.ani.taku_backend.auth.service.OAuth2UserService;
import com.ani.taku_backend.config.filter.JwtAuthenticationFilter;
import com.ani.taku_backend.config.filter.PublicEndpointFilter;
import com.ani.taku_backend.config.filter.RefreshTokenFilter;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import java.util.Arrays;
import java.util.List;

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
    private final PublicEndpointFilter publicEndpointFilter;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers
                .frameOptions(frame -> frame.disable())
            )
            .authorizeHttpRequests(auth -> auth
                // TODO : 개발 과정에서 현재 모든 요청을 허용하고 있음. 추후 권한 관리 필요
                .requestMatchers("/static/**", "/public/**", "/resources/**", "/META-INF/resources/**")
                    .permitAll()
                .requestMatchers("/js/**", "/assets/**", "/css/**")
                    .permitAll()
                .requestMatchers(SecurityPathConfig.PUBLIC_STATIC_PATHS).permitAll()
                .requestMatchers(SecurityPathConfig.PUBLIC_GET_PATHS).permitAll() // GET - post,jangter
                .requestMatchers(HttpMethod.GET, SecurityPathConfig.USER_API_PATH).permitAll()
                .requestMatchers(HttpMethod.POST, SecurityPathConfig.USER_API_PATH).not().hasRole(UserRole.BLACKLIST.name())
                .requestMatchers(HttpMethod.GET, SecurityPathConfig.SHORTS_API_PATH).permitAll()    // 쇼츠 관련 API 허용
                .requestMatchers(SecurityPathConfig.WEBSOCKET_PATHS).permitAll()                    // WebSocket 관련 경로 허용
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(oAuth2UserService)
                )
                .successHandler(this.oAuth2SuccessHandler)
                .failureHandler(this.oAuth2FailureHandler)
            )
            .addFilterBefore(refreshTokenFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(publicEndpointFilter, RefreshTokenFilter.class)
            .addFilterBefore(jwtAuthenticationFilter, PublicEndpointFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 개발 환경에서는 모든 오리진 허용
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        
        // 프로덕션 환경에서는 아래 주석을 해제하고 특정 오리진만 허용
        /*
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:8080",
            "https://localhost:8080",
            "http://localhost:3000",
            "https://localhost:3000",
            "https://api-duckwho.xyz",
            "https://duckwho.vercel.app"
        ));
        */

        List<String> allowedMethods = Arrays.asList(HttpMethod.GET.name(), HttpMethod.POST.name(), HttpMethod.PUT.name(), HttpMethod.DELETE.name(), HttpMethod.OPTIONS.name(), HttpMethod.PATCH.name());

        configuration.setAllowedMethods(allowedMethods);
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        // WebSocket 관련 경로에 대해 동일한 CORS 설정 적용
        for (String path : SecurityPathConfig.WEBSOCKET_PATHS) {
            source.registerCorsConfiguration(path, configuration);
        }
        
        return source;
    }
}
