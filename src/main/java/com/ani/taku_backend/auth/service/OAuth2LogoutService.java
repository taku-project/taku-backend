package com.ani.taku_backend.auth.service;

import com.ani.taku_backend.auth.util.JwtUtil;
import com.ani.taku_backend.common.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class OAuth2LogoutService {

    private final RedisService redisService;
    private final JwtUtil jwtUtil;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${kakao.client-id}")
    private String kakaoClientId;

    @Value("${kakao.logout-redirect-uri}")
    private String logoutRedirectUri;

    /**
     * 일반 로그아웃처리: 카카오 토큰 무효화 및 Redis access token 제거
     */
    public String logout(String accessToken) {
        // TODO: 구글 로그아웃 반영 필요함
        boolean kakaoLogoutSuccess = kakaoCallLogoutAPI(accessToken);

        String email = jwtUtil.getEmailFromToken(accessToken);
        invalidateTokens(email);

        if (kakaoLogoutSuccess) {
            return "카카오 로그아웃 성공 및 토큰 삭제 완료";
        } else {
            return "카카오 로그아웃 실패 및 토큰 삭제됨: 재로그인 필요함";
        }
    }

    /**
     * 레디스에서 토큰들 제거하기
     */
    private void invalidateTokens(String email) {
        redisService.deleteKeyValue("accessToken:"+ email);
        redisService.deleteKeyValue("refreshToken:"+ email);
    }

    /**
     * kakao 일반 로그아웃
     */
    private boolean kakaoCallLogoutAPI(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Object> request = new HttpEntity<>(headers);
        try {
            ResponseEntity<String> response =
                    restTemplate.postForEntity("https://kapi.kakao.com/v1/user/logout", request, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }
}
