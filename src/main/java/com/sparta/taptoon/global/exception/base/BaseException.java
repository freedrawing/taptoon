package com.sparta.taptoon.global.exception.base;

import com.sparta.taptoon.global.exception.enums.ErrorCode;
import lombok.Getter;

@Getter
public class BaseException extends RuntimeException{
  private final ErrorCode errorCode;

  public BaseException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }
}
