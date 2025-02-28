package com.ani.taku_backend.common.Interceptor;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class LoggingInterceptor implements HandlerInterceptor {

    private long startTime;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler){

        startTime = System.currentTimeMillis();

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,Exception ex){


        String client = (request.getUserPrincipal() != null) ? request.getUserPrincipal().getName() : "GUEST";

        String requestUri = request.getRequestURI();
        String requestParams = request.getQueryString();
        int status = response.getStatus();
        long duration = System.currentTimeMillis() - startTime;

        if (requestParams != null) {
            requestParams = URLDecoder.decode(requestParams, StandardCharsets.UTF_8);
        }

        Instant now = Instant.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.of("Asia/Seoul"));
        String formattedTime = formatter.format(now);

        log.info("Time: {}, client: {}, URI: {}, Params: {}, Response: {}, Duration: {}", formattedTime, client, requestUri,requestParams,  status,  duration);


    }
}
