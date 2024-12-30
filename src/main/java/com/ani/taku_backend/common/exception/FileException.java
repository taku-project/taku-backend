package com.ani.taku_backend.common.exception;

/**
 * 파일 관련 I/O 예외 처리
 */
public class FileException extends DuckwhoException {

  public FileException(ErrorCode errorCode) {
    super(errorCode);
  }

  // 파일 업로드 실패
  public static class FileUploadException extends FileException {
    public FileUploadException() {
      super(ErrorCode.FILE_UPLOAD_ERROR);
    }
  }

  // 파일 업로드 실패
  public static class FileNotFoundException extends FileException {
    public FileNotFoundException() {
      super(ErrorCode.FILE_NOT_FOUND);
    }
  }
}
