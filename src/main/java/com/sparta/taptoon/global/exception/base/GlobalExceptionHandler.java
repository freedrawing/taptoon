package com.sparta.taptoon.global.exception.base;

import com.sparta.taptoon.global.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(BaseException.class)
  public ResponseEntity<ApiResponse<?>> handleBusinessException(BusinessException e) {
    log.error("Error Code: {}, Message: {}", e.getErrorCode().getCode(), e.getMessage());

    return ResponseEntity
        .status(e.getErrorCode().getStatus())
        .body(ApiResponse.fail(e.getErrorCode().getStatus(), e.getMessage()).getBody());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Void>> handleValidationExceptions(MethodArgumentNotValidException e) {
    String message = e.getBindingResult().getFieldErrors().stream()
        .map(error -> error.getField() + ": " + error.getDefaultMessage())
        .findFirst().orElse("입력값이 유효하지 않습니다");
    log.error("유효하지 않은 값 입력. message=\"{}\"", message);
    return ApiResponse.fail(HttpStatus.BAD_REQUEST, message);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
    log.error("예상 못한 예외 발생", e);
    return ApiResponse.fail(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
  }

}