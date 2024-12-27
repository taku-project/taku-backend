package com.ani.taku_backend.post.viewcount.helper;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import org.springframework.stereotype.Component;

@Component
public class ProductCookieHelperImpl implements ProductCookieHelper {

    private static final String VIEWED_POST_COOKIE_NAME = "VIEWED_POSTS";
    private static final int COOKIE_EXPIRE_SEC = 60 * 60 * 24; // 1일

    @Override
    public Cookie findCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        return Arrays.stream(request.getCookies())
                .filter(c -> VIEWED_POST_COOKIE_NAME.equals(c.getName()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public boolean hasAlreadyVisited(Cookie cookie, String postId) {
        if (cookie == null || cookie.getValue() == null) {
            return false;
        }
        // 쿠키 값 예시: "123,456,789"
        String[] visitedIds = cookie.getValue().split(",");
        for (String visited : visitedIds) {
            if (visited.equals(postId)) {
                return true; // 이미 본 글
            }
        }
        return false;
    }

    @Override
    public void updateCookie(HttpServletResponse response, Cookie cookie, String postId) {
        if (cookie == null) {
            // 새 쿠키 생성
            cookie = new Cookie(VIEWED_POST_COOKIE_NAME, postId);
        } else {
            // 쿠키 값에 postId 추가
            String newValue = cookie.getValue() + "," + postId;
            cookie.setValue(newValue);
        }
        cookie.setPath("/");
        cookie.setMaxAge(COOKIE_EXPIRE_SEC);
        // 필요 시 cookie.setHttpOnly(true), cookie.setSecure(true) 설정
        response.addCookie(cookie);
    }
}