package com.ani.taku_backend.auth.util;

import com.ani.taku_backend.common.enums.ProviderType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ani.taku_backend.user.model.dto.UserDTO;
import com.ani.taku_backend.user.model.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    private final Key key;
    private final long accessTokenValidityTime;
    private final long temporaryTokenValidityTime;
    private final long refreshTokenValidityTime;
    private final ObjectMapper objectMapper;
    public JwtUtil(
        @Value("${jwt.secret}") String secret,
        @Value("${jwt.access-token-validity}") long accessTokenValidityTime,
        @Value("${jwt.temporary-token-validity}") long temporaryTokenValidityTime,
        @Value("${jwt.refresh-token-validity}") long refreshTokenValidityTime,
        ObjectMapper objectMapper
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenValidityTime = accessTokenValidityTime;
        this.temporaryTokenValidityTime = temporaryTokenValidityTime;
        this.refreshTokenValidityTime = refreshTokenValidityTime;
        this.objectMapper = objectMapper;
    }

    // 임시 토큰 생성 (회원가입용)
    public String createTemporaryToken(Map<String, Object> attributes, ProviderType providerType) {
        Map<String, Object> claims = new HashMap<>();

        // 구글은 젠더, 나이 정보 주지않음 -> null
        switch (providerType) {
            case GOOGLE:
                claims.put("email", attributes.get("email"));
                claims.put("id", String.valueOf(attributes.get("sub")));    // 구글 고유 아이디 -> domesticId
                claims.put("nickname", attributes.get("name"));
                claims.put("profile_image_url", attributes.get("picture"));
                claims.put("type", "TEMPORARY");
                break;
            case KAKAO:
                // 카카오 계정 정보 추출
                Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");

                // 필요한 정보만 claims에 저장
                claims.put("email", kakaoAccount.get("email"));
                claims.put("id", String.valueOf(attributes.get("id"))); // domesticId
                claims.put("gender", String.valueOf(kakaoAccount.get("gender")).toUpperCase());
                claims.put("age_range", String.valueOf(kakaoAccount.get("age_range")));
                claims.put("nickname", ((Map<String, Object>)kakaoAccount.get("profile")).get("nickname"));
                claims.put("profile_image_url", ((Map<String, Object>)kakaoAccount.get("profile")).get("profile_image_url"));
                claims.put("type", "TEMPORARY");
                break;
        }
        
        return createToken(claims, temporaryTokenValidityTime);
    }

    // 액세스 토큰 생성
    public String createAccessToken(User user) {
        UserDTO userDTO = UserDTO.of(user);

        User userEntity = User.builder().userId(userDTO.getUserId())
            .nickname(userDTO.getNickname())
            .email(userDTO.getEmail())
            .role(userDTO.getRole())
            .providerType(userDTO.getProviderType())
            .profileImg(userDTO.getProfileImg())
            .status(userDTO.getStatus())
            .domesticId(userDTO.getDomesticId())
            .gender(userDTO.getGender())
            .ageRange(userDTO.getAgeRange())
            .build();

        Map<String, Object> claims = this.objectMapper.convertValue(userEntity, Map.class);
        claims.put("type", "ACCESS");
        return createToken(claims, accessTokenValidityTime);
    }

    // 리프레시 토큰 생성
    public String createRefreshToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", user.getUserId());
        claims.put("email", user.getEmail());
        claims.put("iat", System.currentTimeMillis() / 1000); // 발행 시간
        claims.put("exp", System.currentTimeMillis() / 1000 + refreshTokenValidityTime); // 만료 시간 (예: 7일)
        claims.put("type", "REFRESH");
        return createToken(claims, refreshTokenValidityTime);
    }

    // 토큰 생성 기본 메소드
    private String createToken(Map<String, Object> claims, long validityTime) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityTime);

        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
    }

    // 토큰에서 카카오 정보 추출
    public Map<String, Object> getKakaoInfoFromToken(String token) {
        Claims claims = extractAllClaims(token);
        
        // 임시 토큰인지 확인
        if (!"TEMPORARY".equals(claims.get("type"))) {
            throw new JwtException("Invalid token type");
        }

        Map<String, Object> kakaoInfo = new HashMap<>();
        kakaoInfo.put("email", claims.get("email"));
        kakaoInfo.put("nickname", claims.get("nickname"));
        
        return kakaoInfo;
    }

    /**
     * 토큰 유효성 검증
     * @param token
     * @return 유효성 검증 결과 (true : 유효, false : 유효하지 않음)
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // 토큰에서 이메일 추출
    public String getEmailFromToken(String token) {
        return extractAllClaims(token).get("email", String.class);
    }

    // 토큰에서 모든 클레임 추출
    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody();
    }

    // 토큰에서 유저 정보 추출
    public User getUserFromToken(String token) {
        Claims claims = extractAllClaims(token);
        UserDTO userDTO = this.objectMapper.convertValue(claims, UserDTO.class);

        User user = User.builder()
            .userId(userDTO.getUserId())
            .email(userDTO.getEmail())
            .nickname(userDTO.getNickname())
            .role(userDTO.getRole())
            .providerType(userDTO.getProviderType())
            .profileImg(userDTO.getProfileImg())
            .status(userDTO.getStatus())
            .domesticId(userDTO.getDomesticId())
            .gender(userDTO.getGender())
            .ageRange(userDTO.getAgeRange())
            .build();
        
        return user;
    }

    // Bearer 토큰에서 실제 토큰 추출
    public String resolveToken(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}