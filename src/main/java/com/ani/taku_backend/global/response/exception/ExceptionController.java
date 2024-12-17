package com.ani.taku_backend.global.response.exception;


import com.ani.taku_backend.global.response.CommonResponse;
import com.ani.taku_backend.global.response.ResponseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class ExceptionController {

    private final ResponseService responseService;

    @ExceptionHandler(InvalidUserExcepetion.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    private CommonResponse invalidateUserException(InvalidUserExcepetion e){
        log.info(e.getMessage());
        return responseService.getErrorResponse(ExceptionMessage.INVALIDATE_USER.getCode(),ExceptionMessage.INVALIDATE_USER.getMessage());

    }
}
