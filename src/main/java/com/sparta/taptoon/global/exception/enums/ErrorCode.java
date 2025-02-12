package com.sparta.taptoon.global.exception.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ErrorCode {

  // Common
  INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "1", "1"),
  RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND,"",""),
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,"",""),

  //user

  //board

  //comment

  //chat

  ;

  private final HttpStatus status;
  private final String code;
  private final String message;
}
