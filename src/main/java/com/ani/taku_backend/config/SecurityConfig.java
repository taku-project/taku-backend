package com.ani.taku_backend.config;

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
//                .requestMatchers("/api/shorts/**", "/api/shorts")
//                    .permitAll()
                .requestMatchers("/js/**", "/assets/**", "/css/**")
                    .permitAll()
                .requestMatchers(SecurityPathConfig.PUBLIC_STATIC_PATHS).permitAll()
                .requestMatchers(SecurityPathConfig.PUBLIC_GET_PATHS).permitAll()                   // GET - post,jangter
                .requestMatchers(HttpMethod.GET, SecurityPathConfig.USER_API_PATH).permitAll()
                .requestMatchers(HttpMethod.POST, SecurityPathConfig.USER_API_PATH).permitAll()
                .requestMatchers(HttpMethod.GET, SecurityPathConfig.SHORTS_API_PATH).permitAll()    // 쇼츠 관련 API 허용
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

        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:8080",
            "https://localhost:8080",
            "http://localhost:3000",
            "https://localhost:3000",
            "https://api-duckwho.xyz",
            "https://duckwho.vercel.app"
        ));

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
