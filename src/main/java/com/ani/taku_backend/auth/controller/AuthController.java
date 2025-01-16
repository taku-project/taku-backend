package com.ani.taku_backend.auth.controller;

import com.ani.taku_backend.auth.service.OAuth2LogoutService;
import com.ani.taku_backend.common.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication API", description = "인증을 위한 API")
@RequiredArgsConstructor
@Log4j2
public class AuthController {

    private final OAuth2LogoutService logoutService;

    /**
     * 로그아웃 요청
     */
    @Operation(
            summary = "유저 로그아웃",
            description = "해당 API 호출되면 로그아웃"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "40100", description = "인증되지 않은 접근"),
            @ApiResponse(responseCode = "40103", description = "잘못된 토큰")
    })
    @PostMapping("/logout")
    public CommonResponse<String> logout(
            @Parameter(description = "인증 토큰 정보 헤더에 전달") @RequestHeader("Authorization") String accessTokenHeader) {
        log.info("로그아웃 컨트롤러 시작");

        // 서비스 레이어에서 예외 발생 시 GlobalExceptionHandler에서 처리
        String logoutMessage = logoutService.logout(accessTokenHeader);
        log.info("로그아웃 성공");

        return CommonResponse.ok(logoutMessage);
    }
}
