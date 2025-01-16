package com.ani.taku_backend.common.exception;

public class PostException extends RuntimeException {
    public PostException(String message) {
        super(message);
    }

    // 게시글 찾을 수 없을 때
    public static class PostNotFoundException extends PostException {
        public PostNotFoundException(String message) {
            super(message);
        }
    }

    public static class PostAccessDeniedException extends PostException {
        public PostAccessDeniedException(String message) {
            super(message);
        }
    }
}
