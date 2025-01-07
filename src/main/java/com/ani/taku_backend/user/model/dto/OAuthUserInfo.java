package com.ani.taku_backend.user.model.dto;

import com.ani.taku_backend.common.enums.ProviderType;
import io.jsonwebtoken.Claims;
import lombok.Data;
import lombok.extern.log4j.Log4j2;


/*
 * OAuth 로그인 시 사용자 정보를 담는 DTO 클래스
 * 
 * 카카오와 구글 로그인을 지원하며, 각 제공자별로 다른 형태의 사용자 정보를 표준화된 형태로 변환
 * 
 * 주요 필드:
 * - email: 사용자 이메일
 * - name: 사용자 이름/닉네임
 * - imageUrl: 프로필 이미지 URL
 * - 추후 계속 추가됨
 * 
 * Claims 객체에서 제공자별 필드를 추출하여 표준화된 형태로 매핑
 * - 카카오: id, email, nickname, profile_image_url 
 * - 구글: sub, email, name, picture
 */
@Data
@Log4j2
public class OAuthUserInfo {
    private String email;
    private String domesticId;
    private String gender;
    private String ageRange;
    private String nickname;
    private String imageUrl;
    private ProviderType providerType;

    public static OAuthUserInfo of(ProviderType provider, Claims claims) {
        switch (provider) {
            case KAKAO:
                return ofKakao(claims);
            case GOOGLE:
                return ofGoogle(claims);
            default:
                throw new IllegalArgumentException("Invalid Provider Type");
        }
    }

    // 카카오 로그인 Claims 처리
    private static OAuthUserInfo ofKakao(Claims claims) {
        OAuthUserInfo userInfo = new OAuthUserInfo();
        userInfo.email = claims.get("email", String.class);
        userInfo.domesticId = claims.get("id", String.class);
        userInfo.nickname = claims.get("nickname", String.class);
        userInfo.gender = claims.get("gender", String.class);
        userInfo.ageRange = claims.get("age_range", String.class);
        userInfo.imageUrl = claims.get("profile_image_url", String.class);
        userInfo.providerType = ProviderType.KAKAO;
        return userInfo;
    }

    // 구글 로그인 Claims 처리
    private static OAuthUserInfo ofGoogle(Claims claims) {
        OAuthUserInfo userInfo = new OAuthUserInfo();
        userInfo.email = claims.get("email", String.class);
        userInfo.domesticId = claims.get("id", String.class);
        userInfo.nickname = claims.get("name", String.class);
        userInfo.imageUrl = claims.get("picture", String.class);
        userInfo.gender = claims.get("gender", String.class);
        userInfo.ageRange = claims.get("age_range", String.class);
        userInfo.providerType = ProviderType.GOOGLE;
        return userInfo;
    }
}
