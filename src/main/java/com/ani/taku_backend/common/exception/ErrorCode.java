package com.ani.taku_backend.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Arrays;


@Getter
@AllArgsConstructor
public enum ErrorCode {
    // Common
    INVALID_INPUT_VALUE(40001, HttpStatus.BAD_REQUEST, "잘못된 입력값입니다."),
    INTERNAL_SERVER_ERROR(50000, HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),
    METHOD_ARGUMENT_TYPE_MISMATCH(40002, HttpStatus.BAD_REQUEST, "잘못된 파라미터 타입입니다."),
    NOT_FOUND_END_POINT(40400, HttpStatus.NOT_FOUND, "존재하지 않는 API입니다."),

    // Database
    DATA_INTEGRITY_VIOLATION(40003, HttpStatus.BAD_REQUEST, "데이터 무결성 위반"),
    DATA_LENGTH_EXCEEDED(40004, HttpStatus.BAD_REQUEST, "데이터 길이 초과"),

    // User
    USER_NOT_FOUND(40401, HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    USER_ALREADY_EXISTS(40901, HttpStatus.CONFLICT, "이미 존재하는 사용자입니다."),
    USER_ALREADY_DELETED(40402, HttpStatus.NOT_FOUND, "이미 삭제된 사용자입니다."),

    // Auth & JWT
    INVALID_TOKEN(40100, HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(40101, HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    UNSUPPORTED_TOKEN(40102, HttpStatus.UNAUTHORIZED, "지원되지 않는 토큰입니다."),
    WRONG_TOKEN(40103, HttpStatus.UNAUTHORIZED, "잘못된 토큰입니다."),
    EMPTY_TOKEN(40104, HttpStatus.UNAUTHORIZED, "토큰이 비어있습니다."),
    UNAUTHORIZED_ACCESS(40105, HttpStatus.UNAUTHORIZED, "인증되지 않은 접근입니다."),
    FORBIDDEN_ACCESS_ADMIN(40300, HttpStatus.FORBIDDEN, "관리자 권한이 필요합니다."),

    // File
    FILE_UPLOAD_ERROR(50300, HttpStatus.SERVICE_UNAVAILABLE, "파일 업로드에 실패했습니다."),
    FILE_DOWNLOAD_ERROR(50301, HttpStatus.SERVICE_UNAVAILABLE, "파일 다운로드에 실패했습니다."),
    FILE_MAX_REGIST_EXCEED(50302, HttpStatus.BAD_REQUEST, "파일 가능 갯수를 초과했습니다."),
    FILE_SIZE_EXCEED(50302, HttpStatus.BAD_REQUEST, "파일 업로드는 최대 50MB까지 가능합니다."),
    FILE_ERROR(50303, HttpStatus.INTERNAL_SERVER_ERROR, "파일 예외가 발생했습니다."),
    FILE_NOT_FOUND(50304, HttpStatus.INTERNAL_SERVER_ERROR, "파일을 찾을 수 없습니다."),
    INVALID_FILE_FORMAT(40005, HttpStatus.BAD_REQUEST, "잘못된 파일 형식입니다."),

    // Category
    NOT_FOUND_CATEGORY(40403, HttpStatus.NOT_FOUND, "존재하지 않는 카테고리입니다."),
    DUPLICATE_CATEGORY_NAME(40902,HttpStatus.CONFLICT, "이미 유사한 이름의 카테고리가 존재합니다."),
    BLACK_USER(40106,HttpStatus.UNAUTHORIZED,"블랙 유저는 카테고리 생성을 할 수 없습니다."),
    NOT_FOUND_GENRE(40404,HttpStatus.NOT_FOUND,"해당하는 장르가 존재하지 않습니다."),

    // Profanity Filter
    NOT_FOUND_PROFANITY_FILTER(40405, HttpStatus.NOT_FOUND, "존재하지 않는 금칙어 필터입니다."),
    INVALID_CONTENT_PROFANITY(40301, HttpStatus.FORBIDDEN, "금칙어가 포함된 내용이 있습니다."),
    DUPLICATE_PROFANITY_FILTER(40903, HttpStatus.CONFLICT, "이미 존재하는 금칙어 필터입니다."),

    // Shorts
    NOT_FOUND_SHORTS(40405, HttpStatus.NOT_FOUND, "존재하지 않는 쇼츠입니다."),
    NOT_FOUND_SHORTS_COMMENT(40406, HttpStatus.NOT_FOUND, "존재하지 않는 댓글입니다."),
    NOT_FOUND_SHORTS_REPLY(40407, HttpStatus.NOT_FOUND, "존재하지 않는 대댓글입니다.");
    // Post / Product
    NOT_FOUND_POST(40403, HttpStatus.NOT_FOUND, "존재하지 않는 글입니다."),

    // Comments
    NOT_FOUND_COMMENTS(40403, HttpStatus.NOT_FOUND, "존재하지 않는 댓글입니다.");


    // 403 Forbidden
    private final Integer code;
    private final HttpStatus httpStatus;
    private final String message;

    public static ErrorCode findByStatus(int statusCode) {
        return Arrays.stream(ErrorCode.values())
                .filter(errorCode -> errorCode.code == statusCode)
                .findFirst()
                .orElse(ErrorCode.INTERNAL_SERVER_ERROR);
    }
}