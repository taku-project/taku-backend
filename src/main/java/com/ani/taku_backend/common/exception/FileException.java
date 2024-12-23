package com.ani.taku_backend.common.exception;

/**
 * 파일 관련 I/O 예외 처리
 */
public class FileException extends RuntimeException {

  public FileException(String message) {
    super(message);
  }

  // 파일 업로드 실패
  public static class FileUploadException extends FileException {
    public FileUploadException(String message) {
      super(message);
    }
  }
}
