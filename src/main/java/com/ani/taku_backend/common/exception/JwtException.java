package com.ani.taku_backend.common.exception;

// JWT 관련 예외
public class JwtException extends RuntimeException {

  public JwtException(String message) {
    super(message);
  }
  
  // 유효하지 않은 토큰인 경우
  public static class InvalidTokenException extends JwtException {
    public InvalidTokenException(String message) {
      super(message);
    }
  }
}
