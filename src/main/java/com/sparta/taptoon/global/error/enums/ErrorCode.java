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
  TOO_MANY_REQUEST(HttpStatus.TOO_MANY_REQUESTS, "GENERAL_429", "현재 요청이 많아 처리할 수 없습니다. 잠시 후 다시 시도해주세요."),

  //member
  MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "Member_404", "존재하지 않는 유저입니다."),
  MEMBER_DELETED(HttpStatus.FORBIDDEN, "Member_403", "삭제된 유저입니다."),
  MEMBER_ALREADY_EXISTS(HttpStatus.CONFLICT, "Member_409", "이미 존재하는 유저입니다."),
  SAME_VALUE_REQUEST(HttpStatus.CONFLICT, "Member_409","이전에 사용하던 정보를 재사용할 수 없습니다."),

  //auth
  INVALID_CREDENTIALS(HttpStatus.BAD_REQUEST,"Auth_400", "올바르지 않은 회원 정보 요청입니다."),
  LOGIN_NOT_ACCEPTABLE(HttpStatus.NOT_ACCEPTABLE,"Auth_406","로그인 요청이 거부되었습니다."),
  NOT_CORRECT_TOKEN_TYPE(HttpStatus.BAD_REQUEST, "Token_400", "올바르지 않은 토큰 타입입니다."),
  EXPIRED_TOKEN(HttpStatus.NOT_ACCEPTABLE,"Token_406", "토큰이 만료되었습니다."),
  INVALID_TOKEN(HttpStatus.BAD_REQUEST,"Token_400", "유효하지 않은 토큰입니다."),
  NOT_FOUND_TOKEN(HttpStatus.NOT_FOUND,"Token_404","존재하지 않는 토큰입니다."),
  //board
  // MatchingPost
  INVALID_ARTIEST_TYPE(HttpStatus.BAD_REQUEST, "MATCHING_POST_400", "잘못된 ArtistType입니다."),
  INVALID_WORK_TYPE(HttpStatus.BAD_REQUEST, "MATCHING_POST_400", "잘못된 WorkType입니다."),
  MATCHING_POST_NOT_FOUND(HttpStatus.NOT_FOUND, "MATCHING_POST_404", "존재하지 않는 매칭 포스트입니다."),



  //portfolio
  PORTFOLIO_NOT_FOUND(HttpStatus.NOT_FOUND,"PORTFOLIO_404", "존재하지 않는 포트폴리오입니다."),
  PORTFOLIO_IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND,"PORTFOLIO_404", "존재하지 않는 포트폴리오 이미지입니다."),
  PORTFOLIO_ACCESS_DENIED(HttpStatus.FORBIDDEN, "PORTFOLIO_403", "포트폴리오 접근 권한이 없습니다."),
  PORTFOLIO_IMAGE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "PORTFOLIO_403", "선택하신 이미지가 포트폴리오에 속해있지 않습니다."),
  CREATION_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "PORTFOLIO_400", "허용된 개수를 초과하였습니다."),

  //comment
  COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND,"COMMENT_404", "존재하지 않는 댓글입니다."),
  COMMENT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "COMMENT_403", "본 댓글에 대한 권한이 없습니다."),

  //chat


  ;

  private final HttpStatus status;
  private final String code;
  private final String message;
}
