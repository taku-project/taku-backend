package com.ani.taku_backend.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * 시스템에서 지원하는 OAuth2 인증 제공자 관리
 */
public enum ProviderType {
  /**
   * 카카오 로그인
   * 카카오 OAuth2를 통한 인증
   */
  KAKAO("kakao"),
  
  /**
   * 네이버 로그인
   * 네이버 OAuth2를 통한 인증
   */
  NAVER("naver"),
  
  /**
   * 구글 로그인
   * 구글 OAuth2를 통한 인증
   */
  GOOGLE("google");

  private final String value;

  /**
   * ProviderType 생성자
   * @param value 제공자 타입을 나타내는 문자열 값
   */
  ProviderType(String value) {
      this.value = value;
  }

  /**
   * 제공자 타입 값 반환
   * @return 제공자 타입을 나타내는 문자열
   */
  public String getValue() {
      return value;
  }

  /**
   * String을 Enum으로 변환하는 메서드
   * @param text 변환할 문자열
   * @return 변환된 Enum
   */
  @JsonCreator
  public static ProviderType fromString(String text) {
      for (ProviderType type : ProviderType.values()) {
          if (type.value.equalsIgnoreCase(text)) {
              return type;
          }
      }
      throw new IllegalArgumentException("지원하지 않는 로그인 제공자입니다: " + text);
  }
}
