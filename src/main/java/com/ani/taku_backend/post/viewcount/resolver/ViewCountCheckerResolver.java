package com.ani.taku_backend.post.viewcount.resolver;


import com.ani.taku_backend.annotation.ViewCountChecker;
import com.ani.taku_backend.post.viewcount.helper.ProductCookieHelper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@RequiredArgsConstructor
public class ViewCountCheckerResolver implements HandlerMethodArgumentResolver {

    private final ProductCookieHelper productCookieHelper;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        // @ViewCountChecker + 파라미터 타입이 Boolean 일 때만 해당
        return parameter.hasParameterAnnotation(ViewCountChecker.class)
                && parameter.getParameterType().equals(Boolean.class);
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) throws Exception {

        HttpServletRequest request  = (HttpServletRequest) webRequest.getNativeRequest();
        HttpServletResponse response = (HttpServletResponse) webRequest.getNativeResponse();

        // URL에서 마지막 슬래시 뒤 숫자를 postId로 간주 (간단 예시)
        String requestURI = request.getRequestURI();
        String postIdString = parsePostIdFromURI(requestURI);

        // 쿠키 찾기
        Cookie cookie = productCookieHelper.findCookie(request);

        // 이미 방문했는지 검사
        boolean alreadyVisited = productCookieHelper.hasAlreadyVisited(cookie, postIdString);
        if (alreadyVisited) {
            // 이미 방문 → 조회수 증가 안 함
            return false;
        }

        // 방문 기록 업데이트(쿠키에 postId 추가)
        productCookieHelper.updateCookie(response, cookie, postIdString);

        // 처음 방문 → 조회수 증가 가능
        return true;
    }

    private String parsePostIdFromURI(String uri) {
        int lastSlash = uri.lastIndexOf("/");
        if (lastSlash == -1) {
            return "";
        }
        return uri.substring(lastSlash + 1);
    }
}