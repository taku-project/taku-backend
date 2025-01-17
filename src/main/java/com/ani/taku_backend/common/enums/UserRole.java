package com.ani.taku_backend.common.enums;

import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 시스템 내의 사용자 권한 레벨을 관리
 */
@Getter
@RequiredArgsConstructor
public enum UserRole implements EnumCode {
  
  /**
   * 일반 사용자 권한
   * 기본적인 서비스 이용이 가능한 일반 사용자
   */
  USER("ROLE_USER"),
  
  /**
   * 관리자 권한
   * 시스템 관리 및 모든 기능에 접근 가능한 관리자
   */
  ADMIN("ROLE_ADMIN"),
  BLACKLIST("ROLE_BLACKLIST"),
  ;

  private final String value;

  public static UserRole findKey(String role) {
    return Arrays.stream(UserRole.values())
            .filter(userRole -> userRole.getValue().equals(role) || userRole.name().equals(role))
            .findFirst()
            .orElseThrow(()-> new DuckwhoException(ErrorCode.INVALID_INPUT_VALUE));

  }
}
