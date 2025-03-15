package com.ani.taku_backend.auth.service;

import com.ani.taku_backend.auth.util.JwtUtil;
import com.ani.taku_backend.common.enums.ProviderType;
import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.service.RedisService;
import com.ani.taku_backend.user.model.entity.User;
import com.ani.taku_backend.user.repository.UserRepository;
import com.ani.taku_backend.user.service.BlackUserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.ani.taku_backend.common.exception.ErrorCode.UNSUPPORTED_PROVIDER;

@Service
@RequiredArgsConstructor
@Log4j2
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final String ACCESS_TOKEN_KEY = "accessToken";

    @Value("${client.prod.registration-url}")
    private String prodRegistrationUrl;

    @Value("${client.dev.registration-url}")
    private String devRegistrationUrl;

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
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

        // 이메일로 찾기 -> domesticId로 찾기로 변경, email이 null 이여도 로그인 됨
        String domesticId;
        switch (providerType) {
            case GOOGLE:
                domesticId = attributes.get("sub").toString();
                break;
            case KAKAO:
                domesticId = attributes.get("id").toString();
                break;
            default:
                throw new DuckwhoException(UNSUPPORTED_PROVIDER);   // 제공하지 않은 OAuth로 인증 시도
        }

        Optional<User> findOptUser = userRepository.findByDomesticId(domesticId);

        // HttpServletRequest 가져오기
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = (requestAttributes != null) ? requestAttributes.getRequest() : null;
        String redirectBaseUrl = (request != null) ? Optional.ofNullable(request.getHeader("Host")).orElse("unknown") : "unknown";
        log.info("redirectBaseUrl: {}", redirectBaseUrl);

        // 유저가 없으면 임시 토큰 생성
        if (findOptUser.isEmpty()) {
            // 임시 토큰 생성
            String temporaryToken = jwtUtil.createTemporaryToken(attributes, providerType);
            String url = redirectBaseUrl.contains("localhost") ? devRegistrationUrl : prodRegistrationUrl;
            log.info("url {}", url);

            // 회원가입 URL 생성
            String redirectUrl = UriComponentsBuilder
                    .fromUriString(url)
                    .queryParam("token", temporaryToken)
                    .queryParam("provider", providerType.name())
                    .build()
                    .toUriString();

            log.info("회원가입 URL: {}", redirectUrl);

            // OAuth2Error 생성 시 description이 아닌 errorCode에 URL을 넣어줍니다
            throw new OAuth2AuthenticationException(
                    new OAuth2Error(
                            "NOT_FOUND_USER", // errorCode에 URL을 넣음
                            "회원가입 페이지 리다이렉트", // description
                            redirectUrl // uri (React 프로젝트 주소)
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
        log.info("OAuth2 attributes: {}", attributes);
        // 유저가 있으면 유저 정보 반환
        DefaultOAuth2User getOAuth2User = null;
        switch (providerType) {
            case GOOGLE:
                getOAuth2User = new DefaultOAuth2User(AuthorityUtils.createAuthorityList(findOptUser.get().getRole().name()), attributes, "sub");
                break;
            case KAKAO:
                getOAuth2User = new DefaultOAuth2User(AuthorityUtils.createAuthorityList(findOptUser.get().getRole().name()), attributes, "id");

                break;
        }
        return getOAuth2User;
    }
}

