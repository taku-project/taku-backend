package com.ani.taku_backend.common.util;

// 레디스 Key 양식을 통일하기 위해 만들어본 클래스
public class RedisKeyUtil {

    private static final String PRODUCT_VIEW_COUNT_PREFIX = "product:viewCount:";

    public static String getViewCountKey(Long productId) {
        return PRODUCT_VIEW_COUNT_PREFIX + productId;   // key생성
    }


    public static String getViewCountPatternKey() {
        return PRODUCT_VIEW_COUNT_PREFIX + "*";   // 조회수 키 패턴 생성, product:viewCount:* -> product:viewCount가 붙은값 모두 가져옴
    }
}
