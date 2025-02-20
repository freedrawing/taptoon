package com.sparta.taptoon.global.error.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ErrorCode {

  // Common
  INVALID_REQUEST(HttpStatus.BAD_REQUEST, "GENERAL_400", "올바르지 않은 요청입니다."),
  METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "GENERAL_405", "잘못된 HTTP 메서드를 호출했습니다."),
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "GENERAL_500", "서버에 에러가 발생했습니다."),
  NOT_FOUND(HttpStatus.NOT_FOUND, "GENERAL_404", "존재하지 않는 엔티티입니다."),
  ENTITY_ALREADY_EXISTS(HttpStatus.CONFLICT, "GENERAL_409", "이미 존재하는 엔티티입니다."),
  ACCESS_DENIED(HttpStatus.FORBIDDEN, "GENERAL_403", "접근 권한이 없습니다."),

  //member
  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_404", "존재하지 않는 유저입니다."),
  USER_DELETED(HttpStatus.FORBIDDEN, "USER_403", "삭제된 유저입니다."),
  USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "USER_409", "이미 존재하는 유저입니다."),

  //board


  //portfolio
  PORTFOLIO_NOT_FOUND(HttpStatus.NOT_FOUND,"PORTFOLIO_404", "존재하지 않는 포트폴리오입니다."),
  PORTFOLIO_IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND,"PORTFOLIO_404", "존재하지 않는 포트폴리오 이미지입니다."),
  PORTFOLIO_ACCESS_DENIED(HttpStatus.FORBIDDEN, "PORTFOLIO_403", "포트폴리오 접근 권한이 없습니다."),
  PORTFOLIO_IMAGE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "PORTFOLIO_403", "선택하신 이미지가 포트폴리오에 속해있지 않습니다."),
  CREATION_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "PORTFOLIO_400", "허용된 개수를 초과하였습니다.")

  //comment

  //chat

  ;

  private final HttpStatus status;
  private final String code;
  private final String message;
}
