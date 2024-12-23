package com.ani.taku_backend.common.aop;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.ani.taku_backend.admin.domain.entity.ProfanityFilter;
import com.ani.taku_backend.admin.service.ProfanityFilterService;
import com.ani.taku_backend.common.annotation.RequireUser;
import com.ani.taku_backend.common.annotation.ValidateProfanity;
import com.ani.taku_backend.common.enums.UserRole;
import com.ani.taku_backend.global.exception.CustomException;
import com.ani.taku_backend.global.exception.ErrorCode;
import com.ani.taku_backend.user.model.dto.PrincipalUser;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class SecurityAspect {

    private final ProfanityFilterService profanityFilterService;

    /**
     * 유저 정보 주입 AOP
     * @param joinPoint
     * @return
     * @throws Throwable
     */
    @Around("@annotation(com.ani.taku_backend.common.annotation.RequireUser)")
    public Object injectUser(ProceedingJoinPoint joinPoint) throws Throwable {

        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();

        RequireUser requireUser = ((MethodSignature) joinPoint.getSignature()).getMethod().getAnnotation(RequireUser.class);

        // Token 인증 정보
        PrincipalUser principalUser = (PrincipalUser)authentication.getPrincipal();
        // 관리자 권한 필요 여부
        checkAdmin(principalUser, requireUser.isAdmin());

        MethodSignature methodSignature = (MethodSignature)joinPoint.getSignature();

        Class<?>[] parameterTypes = methodSignature.getParameterTypes();

        Object[] args = joinPoint.getArgs();

     for (int i = 0; i < parameterTypes.length; i++) {
        if (parameterTypes[i].equals(PrincipalUser.class)) {
            args[i] = principalUser;
        }
     }
        return joinPoint.proceed(args);
    }

    @Around("@annotation(validateProfanity)")
    public Object checkProfanity(ProceedingJoinPoint joinPoint, ValidateProfanity validateProfanity) throws Throwable {
        Object[] args = joinPoint.getArgs();
        List<String> validKeywords = new ArrayList<>();

        // 메소드의 모든 파라미터를 검사
        for (Object arg : args) {
            if (arg == null) continue;
            
            String[] targetFields = validateProfanity.fields();

            if (targetFields.length > 0) {
                checkSpecificFields(arg, targetFields, validKeywords);
            } else {
                checkAllStringFields(arg, validKeywords);
            }
        }

        List<String> allProfanityFilterKeywords = this.profanityFilterService.getAllProfanityFilterKeywords();

        if (!allProfanityFilterKeywords.isEmpty()) {
            for (String inputText : validKeywords) {
                // 공백 제거 및 소문자 변환
                String normalizedText = inputText.replaceAll("\\s+", "").toLowerCase();
                
                // 금칙어 검사
                for (String profanity : allProfanityFilterKeywords) {
                    String normalizedProfanity = profanity.replaceAll("\\s+", "").toLowerCase();
                    if (normalizedText.contains(normalizedProfanity)) {
                        throw new CustomException(ErrorCode.INVALID_CONTENT_PROFANITY);
                    }
                }
            }
        }
        
        return joinPoint.proceed();
    }

    private void checkSpecificFields(Object obj, String[] fieldNames , List<String> validKeywords) {
        for (String fieldName : fieldNames) {
            try {
                Field field = obj.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                Object value = field.get(obj);
                
                if (value instanceof String) {
                    validKeywords.add((String) value);
                }
            } catch (Exception e) {
                log.error("필드 검사 중 오류 발생: {}", e.getMessage());
            }
        }
    }

    private void checkAllStringFields(Object obj, List<String> validKeywords) {
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(obj);
                if (value instanceof String) {
                    validKeywords.add((String) value);
                }
            } catch (Exception e) {
                log.error("필드 검사 중 오류 발생: {}", e.getMessage());
            }
        }
    }

    private void checkAdmin(PrincipalUser principalUser, boolean isAdmin) {
        if (isAdmin && !principalUser.getUser().getRole().equals(UserRole.ADMIN.name())) {
            throw new CustomException(ErrorCode.FORBIDDEN_ACCESS_ADMIN);
        }
    }


    
}
