package com.ani.taku_backend.auth.service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.ani.taku_backend.user.model.entity.User;
import com.ani.taku_backend.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.ani.taku_backend.auth.util.JwtUtil;
import com.ani.taku_backend.common.service.RedisService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final String ACCESS_TOKEN_KEY = "accessToken";

    @Value("${client.web-url}")
    private String webUrl;

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final RedisService redisService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");

        String email = (String) kakaoAccount.get("email");

        // TODO : 제공 플랫폼 추출 (Google, Kakao)
        String provider = userRequest.getClientRegistration().getRegistrationId();

        // User Select
        Optional<User> findOptUser = userRepository.findByEmail(email);

        // 유저가 없으면 임시 토큰 생성
        if (findOptUser.isEmpty()) {
            // 임시 토큰 생성
            String temporaryToken = jwtUtil.createTemporaryToken(attributes);

            // 회원가입 URL 생성
            UriComponentsBuilder redirectUrl = UriComponentsBuilder
                    .fromUriString(webUrl)
                    .queryParam("token", temporaryToken);

            log.info("회원가입 URL: {}", redirectUrl.toUriString());

            // OAuth2Error 생성 시 description이 아닌 errorCode에 URL을 넣어줍니다
            throw new OAuth2AuthenticationException(
                    new OAuth2Error(
                            "NOT_FOUND_USER", // errorCode에 URL을 넣음
                            "신규 회원 가입 필요", // description
                            redirectUrl.toUriString() // uri (React 프로젝트 주소)
                    ));
        }
        try{
            attributes.put("user", findOptUser.get());
        } catch (Exception e) {
            log.error("유저 정보 추출 실패", e);
            throw new OAuth2AuthenticationException("유저 정보 추출 실패");
        } 

        // 유저가 있으면 유저 정보 반환
        return new DefaultOAuth2User(AuthorityUtils.createAuthorityList(findOptUser.get().getRole()), attributes, "id");
    }
}

