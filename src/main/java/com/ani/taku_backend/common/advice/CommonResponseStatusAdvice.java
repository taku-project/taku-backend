package com.ani.taku_backend.common.advice;

import com.ani.taku_backend.common.response.CommonResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/*
해당 클래스의 역할은 CommonResponse를 사용하는 응답의 HttpStatus를 의도한대로 바꾸는 것입니다.
*/

@RestControllerAdvice
public class CommonResponseStatusAdvice implements ResponseBodyAdvice<CommonResponse<?>> {

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return returnType.getParameterType() == CommonResponse.class;
    }

    @Override
    public CommonResponse<?> beforeBodyWrite(
            CommonResponse body,
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