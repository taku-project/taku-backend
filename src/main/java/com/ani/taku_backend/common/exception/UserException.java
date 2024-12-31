package com.ani.taku_backend.common.exception;

public class UserException extends RuntimeException {

  public UserException(String message) {
    super(message);
  }

  // 하위 클래스

  // 이미 가입된 유저에 대한 예외
  public static class UserAlreadyExistsException extends UserException {
    public UserAlreadyExistsException(String message) {
      super(message);
    }
  }

  // 유저를 찾을 수 없는 경우
  public static class UserNotFoundException extends UserException {
    public UserNotFoundException(String message) {
      super(message);
    }

    public UserNotFoundException() {
      super(ErrorCode.USER_NOT_FOUND.getMessage());
    }
  }

  // 이미 삭제된 유저에 대한 예외
  public static class UserAlreadyDeletedException extends UserException {
    public UserAlreadyDeletedException(String message) {
      super(message);
    }
  }
}
