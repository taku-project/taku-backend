package com.ani.taku_backend.auth.controller;

import com.ani.taku_backend.auth.service.OAuth2LogoutService;
import com.ani.taku_backend.common.response.ApiResponse;
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
     * 로그아웃 요청 -> 반환
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(@RequestHeader("Authorization") String accessTokenHeader) {
        log.info("로그아웃 컨트롤러 시작");

        // 서비스 레이어에서 예외 발생 시 GlobalExceptionHandler에서 처리
        String logoutMessage = logoutService.logout(accessTokenHeader);
        log.info("로그아웃 성공");

        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK, true, logoutMessage, null));
    }
}
