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

  // Member
  MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_404", "존재하지 않는 유저입니다."),
  MEMBER_DELETED(HttpStatus.FORBIDDEN, "MEMBER_403", "삭제된 유저입니다."),
  MEMBER_ALREADY_EXISTS(HttpStatus.CONFLICT, "MEMBER_409", "이미 존재하는 유저입니다."),
  SAME_VALUE_REQUEST(HttpStatus.CONFLICT, "MEMBER_409","이전에 사용하던 정보를 재사용할 수 없습니다."),

  // Auth
  INVALID_CREDENTIALS(HttpStatus.BAD_REQUEST,"AUTH_400", "올바르지 않은 회원 정보 요청입니다."),
  LOGIN_NOT_ACCEPTABLE(HttpStatus.NOT_ACCEPTABLE,"AUTH_406","로그인 요청이 거부되었습니다."),
  NOT_CORRECT_TOKEN_TYPE(HttpStatus.BAD_REQUEST, "TOKEN_400", "올바르지 않은 토큰 타입입니다."),
  EXPIRED_TOKEN(HttpStatus.NOT_ACCEPTABLE,"TOKEN_406", "토큰이 만료되었습니다."),
  INVALID_TOKEN(HttpStatus.BAD_REQUEST,"TOKEN_400", "유효하지 않은 토큰입니다."),
  NOT_FOUND_TOKEN(HttpStatus.NOT_FOUND,"TOKEN_404","존재하지 않는 토큰입니다."),

  // MatchingPost
  INVALID_ARTIEST_TYPE(HttpStatus.BAD_REQUEST, "MATCHING_POST_400", "잘못된 ArtistType입니다."),
  INVALID_WORK_TYPE(HttpStatus.BAD_REQUEST, "MATCHING_POST_400", "잘못된 WorkType입니다."),
  MATCHING_POST_NOT_FOUND(HttpStatus.NOT_FOUND, "MATCHING_POST_404", "존재하지 않는 매칭 포스트입니다."),

  // Portfolio
  PORTFOLIO_NOT_FOUND(HttpStatus.NOT_FOUND,"PORTFOLIO_404", "존재하지 않는 포트폴리오입니다."),
  PORTFOLIO_IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND,"PORTFOLIO_404", "존재하지 않는 포트폴리오 이미지입니다."),
  PORTFOLIO_ACCESS_DENIED(HttpStatus.FORBIDDEN, "PORTFOLIO_403", "포트폴리오 접근 권한이 없습니다."),
  PORTFOLIO_IMAGE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "PORTFOLIO_403", "선택하신 이미지가 포트폴리오에 속해있지 않습니다."),
  CREATION_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "PORTFOLIO_400", "허용된 개수를 초과하였습니다."),

  // Comment

  //chat
  CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT_404", "채팅방이 존재하지 않습니다."),
  CHAT_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT_404", "사용자가 존재하지 않습니다."),
  CHAT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "CHAT_403", "이 채팅방에 속해 있지 않은 사용자입니다."),
  CHAT_SELF_INVITATION(HttpStatus.BAD_REQUEST, "CHAT_400", "자기 자신을 초대할 수 없습니다."),
  CHAT_NO_VALID_INVITEES(HttpStatus.BAD_REQUEST, "CHAT_400", "유효한 초대 멤버가 없습니다."),
  CHAT_MINIMUM_MEMBERS(HttpStatus.BAD_REQUEST, "CHAT_400", "채팅방은 최소 2명 이상이어야 합니다."),
  ;

  private final HttpStatus status;
  private final String code;
  private final String message;
}
