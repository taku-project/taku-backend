package com.ani.taku_backend.post.viewcount.helper;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface ProductCookieHelper {

    /**
     * request에서 VIEWED_POST 쿠키를 찾는다.
     */
    Cookie findCookie(HttpServletRequest request);

    /**
     * 이미 방문한 postId인지 체크
     */
    boolean hasAlreadyVisited(Cookie cookie, String postId);

    /**
     * 쿠키를 갱신( postId 추가 )한다
     */
    void updateCookie(HttpServletResponse response, Cookie cookie, String postId);

}
