package com.ani.taku_backend.common.controller;

import com.ani.taku_backend.common.exception.CustomException;
import com.ani.taku_backend.common.exception.ErrorCode;
import com.ani.taku_backend.common.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/success")
    public ApiResponse<String> testSuccess() {
        return ApiResponse.ok("테스트 성공");
    }

    @GetMapping("/error")
    public ApiResponse<String> testError() {
        throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/not-found")
    public ApiResponse<String> testNotFound() {
        throw new CustomException(ErrorCode.NOT_FOUND_END_POINT);
    }

    @GetMapping("/user-not-found/{id}")
    public ApiResponse<String> testUserNotFound(@PathVariable Long id) {
        throw new CustomException(ErrorCode.USER_NOT_FOUND);
    }

    @GetMapping("/invalid-token")
    public ApiResponse<String> testInvalidToken() {
        throw new CustomException(ErrorCode.INVALID_TOKEN);
    }

    @GetMapping("/file-upload")
    public ApiResponse<String> testFileUpload() {
        throw new CustomException(ErrorCode.FILE_UPLOAD_ERROR);
    }

    @GetMapping("/invalid-input")
    public ApiResponse<String> testInvalidInput() {
        throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
    }
}