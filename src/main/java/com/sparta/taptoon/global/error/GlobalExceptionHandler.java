package com.sparta.taptoon.global.error;

import com.sparta.taptoon.global.common.ApiResponse;
import com.sparta.taptoon.global.error.enums.ErrorCode;
import com.sparta.taptoon.global.error.exception.BaseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestValueException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static com.sparta.taptoon.global.error.enums.ErrorCode.*;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // `RequestBody` 잘못된 값 들어왔을 때
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ApiResponse<Void>> handleValidationExceptions(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst().orElse("입력값이 유효하지 않습니다");

        log.error("유효하지 않은 값 입력. message=\"{}\"", message);
        return ApiResponse.fail(HttpStatus.BAD_REQUEST, message);
    }

    // 잘못된 HttpMethod 요청 왔을 때 (컨트롤러에서 정의되지 않은 Http 메서드 요청할 때)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ResponseEntity<ApiResponse<?>> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.error("Error Code: {}, Message: {}", METHOD_NOT_ALLOWED.getCode(), METHOD_NOT_ALLOWED.getMessage());

        return ResponseEntity
                .status(METHOD_NOT_ALLOWED.getStatus())
                .body(ApiResponse.fail(METHOD_NOT_ALLOWED.getStatus(), METHOD_NOT_ALLOWED.getMessage()).getBody());
    }

    // 파라미터, 혹은, Body에 잘못된 양식의 값이 들어왔을 때
    @ExceptionHandler({
            MethodArgumentTypeMismatchException.class, // PathVariable이나 RequestParam의 타입 변환 실패 시
            HttpMessageConversionException.class, // JSON to Object 변환 실패 || Request Body 데이터 파싱 실패 || JSON 형식이 잘못된 경우
            MissingRequestValueException.class // 필수 파라미터가 누락된 경우
    })
    protected ResponseEntity<ApiResponse<?>> handleInvalidInputException(RuntimeException e) {
        log.error("Invalid Input Exception", e);

        return ResponseEntity
                .status(INVALID_REQUEST.getStatus())
                .body(ApiResponse.fail(INVALID_REQUEST.getStatus(), INVALID_REQUEST.getMessage()).getBody());
    }

    // 기본 비즈니스 로직에서 예외 발생했을 때
    @ExceptionHandler(BaseException.class)
    protected ResponseEntity<ApiResponse<?>> handleBusinessException(BaseException e) {
        log.error("Error Code: {}, Message: {}", e.getErrorCode().getCode(), e.getMessage());

        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(ApiResponse.fail(e.getErrorCode().getStatus(), e.getMessage()).getBody());
    }

    // 예상 못한 예외
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("예상 못한 예외 발생", e);
        return ApiResponse.fail(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }

}