package com.ani.taku_backend.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ErrorCode {
    // Test Error
    TEST_ERROR(10000, HttpStatus.BAD_REQUEST, "테스트 에러입니다."),
    // 404 Not Found
    NOT_FOUND_END_POINT(40400, HttpStatus.NOT_FOUND, "존재하지 않는 API입니다."),
    NOT_FOUND_GENRE(40401,HttpStatus.NOT_FOUND,"해당하는 장르가 존재하지 않습니다."),

    NOT_FOUND_PROFANITY_FILTER(40401, HttpStatus.NOT_FOUND, "존재하지 않는 금칙어 필터입니다."),

    // 403 Forbidden
    FORBIDDEN_ACCESS_ADMIN(40300, HttpStatus.FORBIDDEN, "관리자 권한이 필요합니다."),
    INVALID_CONTENT_PROFANITY(40301, HttpStatus.FORBIDDEN, "금칙어가 포함된 내용이 있습니다."),

    // 400 Bad Request
    INVALID_INPUT_VALUE(40001, HttpStatus.BAD_REQUEST, "유효하지 않은 입력값입니다."),

    // 409 Conflict
    DUPLICATE_PROFANITY_FILTER(40900, HttpStatus.CONFLICT, "이미 존재하는 금칙어 필터입니다."),

    // 500 Internal Server Error
    INTERNAL_SERVER_ERROR(50000, HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류입니다."),
    INVALIDATE_IMAGE_UPLOAD(50001, HttpStatus.INTERNAL_SERVER_ERROR, "이미지 업로드 실패"),

    INVALIDATE_USER(1000,HttpStatus.NOT_FOUND,"해당하는 사용자가 없습니다."),
    INVALIDATE_GOODS(1001,HttpStatus.NOT_FOUND,"해당하는 상품이 존재하지 않습니다."),

    BLACK_USER(4001,HttpStatus.UNAUTHORIZED,"블랙 유저는 카테고리 생성을 할 수 없습니다."),
    INVALIDATE_IMAGE(4002,HttpStatus.BAD_REQUEST,"이미지 파일이 아닙니다."),
    DUPLICATE_CATEGORY_NAME(4003,HttpStatus.BAD_REQUEST, "이미 유사한 이름의 카테고리가 존재합니다."),
    NOT_FOUND_CATEGORY(4004,HttpStatus.NOT_FOUND, "존재하지 않는 카테고리입니다.");


    private final Integer code ;
    private final HttpStatus httpStatus;
    private final String message;


}
