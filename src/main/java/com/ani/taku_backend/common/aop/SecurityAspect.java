package com.ani.taku_backend.common.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.ani.taku_backend.user.model.dto.PrincipalUser;

@Aspect
@Component
public class SecurityAspect {

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

        PrincipalUser principalUser = (PrincipalUser)authentication.getPrincipal();

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
    
}
