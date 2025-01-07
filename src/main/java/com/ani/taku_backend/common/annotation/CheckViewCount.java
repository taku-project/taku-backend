package com.ani.taku_backend.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.ani.taku_backend.common.enums.ViewType;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckViewCount {
    ViewType viewType() default ViewType.SHORTS;
    int expireTime() default 30;    // 분 단위 설정
    String targetId() default "";
}
