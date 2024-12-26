package com.ani.taku_backend.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.PARAMETER}) // 메소드와 파라미터에 사용 가능
@Retention(RetentionPolicy.RUNTIME) // 런타임에도 어노테이션 정보가 유지되도록 설정
public @interface ValidateProfanity {
    String[] fields() default {}; // 특정 필드만 검사하고 싶을 때 사용
    boolean throwException() default true; // 금칙어 발견 시 예외 발생 여부
}