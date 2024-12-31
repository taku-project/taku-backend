package com.ani.taku_backend.config;

import org.springframework.util.AntPathMatcher;
import java.util.Arrays;

public class SecurityPathConfig {
    private static final AntPathMatcher pathMatcher = new AntPathMatcher();

    // 인증이 필요없는 정적 리소스 경로
    public static final String[] PUBLIC_STATIC_PATHS = {
        // TODO 개발 완료시 /admin/** 제거 
        "/admin/**",
        "/",
        "/login",
        "/oauth2/authorization/**",
        "/login/oauth2/code/**",
        "/h2-console/**",
        "/swagger-ui/**",
        "/v3/api-docs/**",
        "/swagger-ui.html",
        "/swagger-resources/**",
        "/webjars/**",
        // 정적 리소스
        "/static/**",
        "/public/**",
        "/resources/**",
        "/META-INF/resources/**",
        "/shorts/**"
    };

    // 사용자 API 관련 설정
    public static final String USER_API_PATH = "/api/user/**";

    // 쇼츠 API 관련 설정
    public static final String SHORTS_API_PATH = "/api/shorts/**";
    
    public static boolean isPermitAllPath(String path) {
        return Arrays.stream(PUBLIC_STATIC_PATHS)
            .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    // 사용자 API 관련 설정
    public static boolean isUserApiPath(String path, String method) {
        return pathMatcher.match(USER_API_PATH, path) && 
            (method.equals("GET") || method.equals("POST"));
    }

    // 쇼츠 API 관련 설정
    public static boolean isShortsApiPath(String path, String method) {
        return pathMatcher.match(SHORTS_API_PATH, path) && 
            (method.equals("GET"));
    }

    public static boolean shouldSkipFilter(String path, String method) {
        return isPermitAllPath(path) || isUserApiPath(path, method) || isShortsApiPath(path, method);
    }
}
