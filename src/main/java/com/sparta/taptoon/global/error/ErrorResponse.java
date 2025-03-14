package com.sparta.taptoon.global.error;

import com.sparta.taptoon.global.error.enums.ErrorCode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ErrorResponse {

    private String message;
    private String code;

    private ErrorResponse(final ErrorCode errorCode) {
        this.message = errorCode.getMessage();
        this.code = errorCode.getCode();
    }

    private ErrorResponse(final ErrorCode errorCode, final String message) {
        this.message = message;
        this.code = errorCode.getCode();
    }

    public static ErrorResponse of(final ErrorCode errorCode) {
        return new ErrorResponse(errorCode);
    }

    public static ErrorResponse of(final ErrorCode errorCode, final String message) {
        return new ErrorResponse(errorCode, message);
    }
}