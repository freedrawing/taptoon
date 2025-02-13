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

  // MatchingPost
  INVALID_ARTIEST_TYPE(HttpStatus.BAD_REQUEST, "MATCHING_POST_400", "잘못된 ArtistType입니다."),
  INVALID_WORK_TYPE(HttpStatus.BAD_REQUEST, "MATCHING_POST_400", "잘못된 WorkType입니다."),
  MATCHING_POST_NOT_FOUND(HttpStatus.NOT_FOUND, "MATCHING_POST_404", "존재하지 않는 매칭 포스트입니다."),


  //comment

  //chat

  ;

  private final HttpStatus status;
  private final String code;
  private final String message;
}
