package com.sparta.taptoon.global.error.exception;

import com.sparta.taptoon.global.error.enums.ErrorCode;

public class TooManyRequestsException extends BaseException{

    public TooManyRequestsException(ErrorCode errorCode) {
        super(errorCode);
    }

    public TooManyRequestsException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }

    public TooManyRequestsException() {
        super(ErrorCode.TOO_MANY_REQUEST);
    }
}
