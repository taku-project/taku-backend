package com.ani.taku_backend.auth.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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

    public JwtUtil(
        @Value("${jwt.secret}") String secret,
        @Value("${jwt.access-token-validity}") long accessTokenValidityTime,
        @Value("${jwt.temporary-token-validity}") long temporaryTokenValidityTime
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenValidityTime = accessTokenValidityTime;
        this.temporaryTokenValidityTime = temporaryTokenValidityTime;
    }

    // 임시 토큰 생성 (회원가입용)
    public String createTemporaryToken(Map<String, Object> attributes) {
        Map<String, Object> claims = new HashMap<>();
        
        // 카카오 계정 정보 추출
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        
        // 필요한 정보만 claims에 저장
        claims.put("email", kakaoAccount.get("email"));
        claims.put("nickname", ((Map<String, Object>)kakaoAccount.get("profile")).get("nickname"));
        claims.put("type", "TEMPORARY");
        
        return createToken(claims, temporaryTokenValidityTime);
    }

    // 액세스 토큰 생성
    public String createAccessToken(String email, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", email);
        claims.put("role", role);
        claims.put("type", "ACCESS");
        
        return createToken(claims, accessTokenValidityTime);
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

    // 토큰 유효성 검증
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
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody();
    }

    // Bearer 토큰에서 실제 토큰 추출
    public String resolveToken(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}