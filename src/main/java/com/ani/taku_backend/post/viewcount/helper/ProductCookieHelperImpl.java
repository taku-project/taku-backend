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
    private static final String SEPARATOR = "-";  // 쿠키 값 구분자
    private static final int MAX_COOKIE_LENGTH = 3500; // 쿠키 최대 길이 (4KB 제한보다 작게 설정)
    private static final int MAX_VIEWED_POSTS = 100; // 최대 저장할 게시글 수

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
        // 쿠키 값 예시: "123-456-789"
        String[] visitedIds = cookie.getValue().split(SEPARATOR);
        for (String visited : visitedIds) {
            if (visited.equals(postId)) {
                return true; // 이미 본 글
            }
        }
        return false;
    }

    @Override
    public void updateCookie(HttpServletResponse response, Cookie cookie, String postId) {
        String newValue;
        if (cookie == null) {
            // 새 쿠키 생성
            newValue = postId;
        } else {
            // 기존 값 파싱
            String[] visitedIds = cookie.getValue().split(SEPARATOR);
            
            // 이미 최대 개수에 도달했다면 가장 오래된 항목 제거
            if (visitedIds.length >= MAX_VIEWED_POSTS) {
                visitedIds = Arrays.copyOfRange(visitedIds, 1, visitedIds.length);
            }
            
            // 새로운 값 추가
            String[] newVisitedIds = Arrays.copyOf(visitedIds, visitedIds.length + 1);
            newVisitedIds[newVisitedIds.length - 1] = postId;
            
            // 배열을 문자열로 변환
            newValue = String.join(SEPARATOR, newVisitedIds);
            
            // 쿠키 크기 제한 체크
            if (newValue.length() > MAX_COOKIE_LENGTH) {
                // 크기 초과시 마지막 항목만 유지
                newValue = postId;
            }
        }
        
        cookie = new Cookie(VIEWED_POST_COOKIE_NAME, newValue);
        cookie.setPath("/");
        cookie.setMaxAge(COOKIE_EXPIRE_SEC);
        cookie.setHttpOnly(true);  // JavaScript에서 접근 불가능하게 설정
        cookie.setSecure(false);   // 개발 환경에서는 false로 설정
        response.addCookie(cookie);
    }
}