package com.ani.taku_backend.auth.service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.ani.taku_backend.common.enums.ProviderType;
import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.user.model.entity.User;
import com.ani.taku_backend.user.repository.UserRepository;
import com.ani.taku_backend.user.service.BlackUserService;
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

import static com.ani.taku_backend.common.exception.ErrorCode.UNSUPPORTED_PROVIDER;

@Service
@RequiredArgsConstructor
@Log4j2
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final String ACCESS_TOKEN_KEY = "accessToken";


    @Value("${client.registration-url}")
    private String registrationUrl;

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final RedisService redisService;
    private final BlackUserService blackUserService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());

        // 제공되는 OAuth 플랫폼 추출하기
        ProviderType providerType;
        try {
            providerType = ProviderType.fromString(userRequest.getClientRegistration().getRegistrationId());
        } catch (IllegalArgumentException e) {
            throw new DuckwhoException(UNSUPPORTED_PROVIDER);   // 제공하지 않은 OAuth로 인증 시도
        }

        // 이메일 구하기
        String email;
        switch (providerType) {
            case GOOGLE:
                email = (String) attributes.get("email");
                break;
            case KAKAO:
                Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
                email = (String) kakaoAccount.get("email");
                break;
            default:
                throw new DuckwhoException(UNSUPPORTED_PROVIDER);   // 제공하지 않은 OAuth로 인증 시도
        }

        // User Select
        Optional<User> findOptUser = userRepository.findByEmail(email);

        // 유저가 없으면 임시 토큰 생성
        if (findOptUser.isEmpty()) {
            // 임시 토큰 생성
            String temporaryToken = jwtUtil.createTemporaryToken(attributes, providerType); // 프로바이더 주입

            // 회원가입 URL 생성
            UriComponentsBuilder redirectUrl = UriComponentsBuilder
                    .fromUriString(registrationUrl);

            log.info("회원가입 URL: {}", redirectUrl.toUriString());

            // OAuth2Error 생성 시 description이 아닌 errorCode에 URL을 넣어줍니다
            throw new OAuth2AuthenticationException(
                    new OAuth2Error(
                            "NOT_FOUND_USER", // errorCode에 URL을 넣음
                            temporaryToken, // description
                            redirectUrl.toUriString() // uri (React 프로젝트 주소)
                    ));

        }
        boolean isBlack = this.blackUserService.findByUserId(findOptUser.get().getUserId()).isEmpty() ? false : true;

        try {
            attributes.put("user", findOptUser.get());
            attributes.put("is_black", isBlack);
        } catch (Exception e) {
            log.error("유저 정보 추출 실패", e);
            throw new OAuth2AuthenticationException("유저 정보 추출 실패");
        }

        // 유저가 있으면 유저 정보 반환
        DefaultOAuth2User getOAuth2User = null;
        switch (providerType) {
            case GOOGLE:
                getOAuth2User = new DefaultOAuth2User(AuthorityUtils.createAuthorityList(findOptUser.get().getRole()), attributes, "sub");
                break;
            case KAKAO:
                getOAuth2User = new DefaultOAuth2User(AuthorityUtils.createAuthorityList(findOptUser.get().getRole()), attributes, "id");
                break;
        }
        return getOAuth2User;
    }
}

