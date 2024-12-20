package com.ani.taku_backend.global.advice;

import com.ani.taku_backend.global.response.ApiResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/*
해당 클래스의 역할은  ApiResponse를 사용하는 응답의 HttpStatus를 의도한대로 바꾸는 것입니다.
*/

@RestControllerAdvice
public class ApiResponseStatusAdvice implements ResponseBodyAdvice<ApiResponse<?>> {

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return returnType.getParameterType() == ApiResponse.class;
    }

    @Override
    public ApiResponse<?> beforeBodyWrite(
            ApiResponse body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response
    ) {
        HttpStatus status = body.httpStatus();
        response.setStatusCode(status);

        return body;
    }
}