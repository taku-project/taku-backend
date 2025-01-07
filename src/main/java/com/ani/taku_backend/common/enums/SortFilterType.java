package com.ani.taku_backend.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 정렬 기준을 관리
 */
public enum SortFilterType {

    /**
     * 최신순 - 커뮤니티 글, 상품 적용
     */
    LATEST("latest"),

    /**
     * 조회수 기준 - 커뮤니티 글 적용
     */
    VIEWS("views"),

    /**
     * 좋아요 기준 - 커뮤니티 글 적용
     */
    LIKES("likes"),

    /**
     * 가격 내림차순 - 상품 적용
     */
    PRICE_DESC("price_desc"),

    /**
     * 가격 오름차순 - 상품 적용
     */
    PRICE_ASC("price_asc");

    private final String value;

    /**
     * 생성자
     * @param value 필터 기준 문자열 값
     *
     */
    SortFilterType(String value) {
        this.value = value;
    }

    /**
     * 기준 반환
     * @return 필터 기준 문자열
     */
    public String getValue() {
        return value;
    }
}
