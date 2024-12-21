package com.ani.taku_backend.common.exception;

import com.ani.taku_backend.common.ApiConstants;
import com.ani.taku_backend.common.model.MainResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.net.BindException;

@ControllerAdvice
public class GlobalExceptionHandler {

    // SQL 관련
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<MainResponse<Void>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        Throwable rootCause = ex.getRootCause();
        String errorMessage = (rootCause != null ? rootCause.getMessage() : ex.getMessage());
        String parseMessage = parseColumnNameAndMessage(errorMessage);

        return ResponseEntity.badRequest().body(
                new MainResponse<>(
                        ApiConstants.Status.ERROR,
                        parseMessage != null ? parseMessage : ApiConstants.Message.BAD_REQUEST
                )
        );
    }

    // UserNotFoundException , UserAlreadyDeletedException
    // 204 : No Content
    @ExceptionHandler({UserException.UserNotFoundException.class, UserException.UserAlreadyDeletedException.class})
    public ResponseEntity<MainResponse<Void>> handleUserNotFoundException(UserException.UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new MainResponse<>(
                        ApiConstants.Status.ERROR,
                        ex.getMessage()
                )
        );
    }

    // UserAlreadyExistsException
    @ExceptionHandler(UserException.UserAlreadyExistsException.class)
    public ResponseEntity<MainResponse<Void>> handleUserAlreadyExistsException(UserException.UserAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                new MainResponse<>(
                        ApiConstants.Status.ERROR,
                        ex.getMessage()
                )
        );
    }

    // InvalidTokenException
    @ExceptionHandler(JwtException.InvalidTokenException.class)
    public ResponseEntity<MainResponse<Void>> handleInvalidTokenException(JwtException.InvalidTokenException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new MainResponse<>(
                        ApiConstants.Status.ERROR,
                        ex.getMessage()
                )
        );
    }

    // FileUploadException
    @ExceptionHandler(FileException.FileUploadException.class)
    public ResponseEntity<MainResponse<Void>> handleFileUploadException(FileException.FileUploadException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
                new MainResponse<>(
                        ApiConstants.Status.ERROR,
                        ex.getMessage()
                )
        );
    }

    // 공통 Exception
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<MainResponse<Void>> handleException(Exception ex) {
        return ResponseEntity.badRequest().body(
                new MainResponse<>(
                        ApiConstants.Status.ERROR,
                        ex.getMessage() != null ? ex.getMessage() : ApiConstants.Message.BAD_REQUEST
                )
        );
    }

    public String parseColumnNameAndMessage(String errorMessage) {
        String columnName = "undefined";

        // H2 메시지에서 컬럼명 추출(길이초과)
        if (errorMessage.contains("Value too long for column")) {
            int startIndex = errorMessage.indexOf("\"") + 1;
            int endIndex = errorMessage.indexOf(" ", startIndex);
            if (startIndex > 0 && endIndex > startIndex) {
                columnName = errorMessage.substring(startIndex, endIndex).trim();
            }
            errorMessage = "입력된 데이터가 허용된 길이를 초과했습니다. (컬럼명: " + columnName.toLowerCase() + ")";
        }

        // MySQL 메시지에서 컬럼명 추출(길이초과)
        else if (errorMessage.contains("Data too long for column")) {
            int startIndex = errorMessage.indexOf("'") + 1;
            int endIndex = errorMessage.indexOf("'", startIndex);
            if (startIndex > 0 && endIndex > startIndex) {
                columnName = errorMessage.substring(startIndex, endIndex).trim();
            }
            errorMessage = "입력된 데이터가 허용된 길이를 초과했습니다. (컬럼명: " + columnName.toLowerCase() + ")";
        }

        return errorMessage;
    }

    /**
     * 파라미터 타입 불일치 예외
     * ex - ?id = abc 이면 해당 예외가 반환됨
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<MainResponse<Void>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        String parameterName = ex.getName();
        String requiredType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "올바른 타입";
        String errorMessage = String.format("'%s' 파라미터의 값이 잘못되었습니다. (%s 타입이 필요합니다.)", parameterName, requiredType);

        return ResponseEntity.badRequest().body(
                new MainResponse<>(
                        ApiConstants.Status.ERROR,
                        errorMessage
                )
        );
    }

}