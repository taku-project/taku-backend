package com.ani.taku_backend.auth.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ani.taku_backend.common.enums.ProviderType;
import com.ani.taku_backend.common.model.MainResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;


@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication API", description = "인증을 위한 API")
public class AuthController {

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String kakaoClientId;
    
    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String kakaoRedirectUri;
  
    @Operation(summary = "로그인 URL 생성", description = "로그인 URL을 생성합니다. 반환 값(SUCCESS/FAIL)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "URL Response : SUCCESS"),
            @ApiResponse(responseCode = "400", description = "Bad Request: Invalid input data.")
    })
    @Parameter(name = "provider", description = "로그인 제공자", required = true, 
        schema = @Schema(type = "string", allowableValues = {"kakao", "google"}))
    @GetMapping("/{provider}/url")
    public ResponseEntity<MainResponse<String>> getLoginUrl(@PathVariable("provider") String providerStr) {
        ProviderType provider = ProviderType.fromString(providerStr);
        
        String url;
        
        switch (provider) {
            case KAKAO:
                url = String.format(
                    "https://kauth.kakao.com/oauth/authorize?client_id=%s&redirect_uri=%s&response_type=code",
                    kakaoClientId,
                    kakaoRedirectUri
                );
                break;
            case GOOGLE:
                // 구글 로그인 URL 구성
                url = "구글_로그인_URL";
                break;
            default:
                throw new IllegalArgumentException("not supported provider: " + provider);
        }
        
        return ResponseEntity.ok(MainResponse.getSuccessResponse(url));
    }
    
}
