package com.ani.taku_backend.common.enums;

/**
 * 시스템 내의 사용자 권한 레벨을 관리
 */
public enum UserRole {
  
  /**
   * 일반 사용자 권한
   * 기본적인 서비스 이용이 가능한 일반 사용자
   */
  USER("ROLE_USER"),
  
  /**
   * 관리자 권한
   * 시스템 관리 및 모든 기능에 접근 가능한 관리자
   */
  ADMIN("ROLE_ADMIN");

  private final String value;

  /**
   * UserRole 생성자
   * @param value 권한을 나타내는 문자열 값
   */
  UserRole(String value) {
      this.value = value;
  }

  /**
   * 권한 값 반환
   * @return 권한을 나타내는 문자열
   */
  public String getValue() {
      return value;
  }
}
