package com.ani.taku_backend.common.aop;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import com.ani.taku_backend.common.annotation.CheckViewCount;
import com.ani.taku_backend.common.enums.ViewType;
import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.exception.ErrorCode;
import com.ani.taku_backend.common.service.RedisService;
import com.ani.taku_backend.user.model.dto.PrincipalUser;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class ViewCountAspect {

    private static final String GUEST_ID_COOKIE_NAME = "guest_id";

    private static final String VIEW_COUNT_KEY = "view_count";

    private static final int EXPIRE_DAY = (int) Duration.ofDays(1).toSeconds();

    private final RedisService redisService;


    @Around("@annotation(com.ani.taku_backend.common.annotation.CheckViewCount)")
    public Object checkAnonymous(ProceedingJoinPoint joinPoint) throws Throwable {
        // 현재 요청 정보 가져오기
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getResponse();

        String userId = null;
        ViewType viewType = getViewType(joinPoint);
        String targetId = getTargetId(joinPoint);
        // 익명 사용자인지 체크
        boolean isAnonymous = checkAnonymous();

        isAnonymous = true;
        if(isAnonymous) {
            // 인증 사용자인 경우 쿠키 체크
            Cookie cookie = Optional.ofNullable(findCookie(request)).orElseGet(() -> {
                log.info("쿠키가 없습니다.");
                // 쿠기가 없으면 쿠키 생성
                return setCookie(response, UUID.randomUUID().toString(), EXPIRE_DAY);
            });
            // 쿠키 값 추출 (비 로그인 유저)
            userId = cookie.getValue();
        }else{
            // 로그인 유저인 경우 유저 아이디 추출
            userId = getUserId();
        }
        
        Optional.ofNullable(userId).orElseThrow(() -> new DuckwhoException(ErrorCode.USER_NOT_FOUND));

        // ex) view_count:shorts:{shorts_id}:{userId} -> 1
        String key = String.format("%s:%s:%s:%s", VIEW_COUNT_KEY, viewType.getValue(), targetId, userId);

        String value = this.redisService.getKeyValue(key);
        
        boolean isFirstView = false;
        if(value == null) {
            this.redisService.setKeyValue(key, "1", Duration.ofMinutes(getExpireTime(joinPoint)));
            isFirstView = true;
            log.info("레디스 저장 완료");
        }else{
            log.info("레디스 조회 했는데 있음!!");
        }

        // 메소드의 파라미터 타입들을 가져옵니다
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Class<?>[] parameterTypes = methodSignature.getParameterTypes();
        
        // 메소드의 인자들을 가져옵니다
        Object[] args = joinPoint.getArgs();
        
        // 파라미터 타입을 순회하면서 boolean 타입을 찾아 isFirstView 값을 주입합니다
        for (int i = 0; i < parameterTypes.length; i++) {
            if (parameterTypes[i].equals(boolean.class) || parameterTypes[i].equals(Boolean.class)) {
                args[i] = isFirstView;
            }
        }
        
        return joinPoint.proceed(args);
    }

    private Cookie findCookie(HttpServletRequest request) {

        Cookie[] cookies = request.getCookies();
        if(cookies == null){
            return null;
        }
        return Arrays.stream(cookies).filter(cookie -> cookie.getName().equals(GUEST_ID_COOKIE_NAME)).findFirst().orElse(null);
    }

    private Cookie setCookie(HttpServletResponse response, String userId , int expireTime) {
        Cookie cookie = new Cookie(GUEST_ID_COOKIE_NAME, userId);
        cookie.setMaxAge(expireTime);
        response.addCookie(cookie);
        log.info("쿠키 생성 완료");
        return cookie;
    }

    /**
     * 뷰 타입 추출
     * @param joinPoint
     * @return
     */
    private ViewType getViewType(ProceedingJoinPoint joinPoint) {
        CheckViewCount checkViewCount = ((MethodSignature) joinPoint.getSignature()).getMethod().getAnnotation(CheckViewCount.class);
        return checkViewCount.viewType();
    }

    /**
     * 뷰 타입 추출
     * @param joinPoint
     * @return
     */
    private int getExpireTime(ProceedingJoinPoint joinPoint) {
        CheckViewCount checkViewCount = ((MethodSignature) joinPoint.getSignature()).getMethod().getAnnotation(CheckViewCount.class);
        return checkViewCount.expireTime();
    }

    /**
     * 뷰상 ID 추출 및 타입 변환
     * @param joinPoint
     * @return
     */
    private String getTargetId(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        CheckViewCount checkViewCount = signature.getMethod().getAnnotation(CheckViewCount.class);
        
        // SpEL 처리를 위한 설정
        StandardEvaluationContext context = new StandardEvaluationContext();
        ExpressionParser parser = new SpelExpressionParser();
        
        // 메소드 파라미터 이름과 값을 context에 추가
        String[] paramNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();
        for (int i = 0; i < paramNames.length; i++) {
            context.setVariable(paramNames[i], args[i]);
        }
        
        // SpEL을 이용해 실제 값 추출
        Expression expression = parser.parseExpression(checkViewCount.targetId());
        String targetId = expression.getValue(context, String.class);
        
        if (targetId == null) {
            throw new DuckwhoException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
        
        return targetId;
    }


    /**
     * 익명 사용자인지 체크
     * @return true : 익명 사용자, false : 인증 사용자
     */
    private boolean checkAnonymous() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return true;
        }
        return false;
    }

    private String getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }

        PrincipalUser principalUser = (PrincipalUser)authentication.getPrincipal();
        // 쿠키는 문자열로 저장되기 때문에 Long 타입을 문자열로 변환
        Long userId = principalUser.getUserId();
        return userId.toString();
    }

}
