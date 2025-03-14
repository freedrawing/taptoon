package com.sparta.taptoon.global.error.exception;

import com.sparta.taptoon.global.error.enums.ErrorCode;

public class InvalidRequestException extends BaseException {

    public InvalidRequestException(ErrorCode errorCode) {
        super(errorCode);
    }

    public InvalidRequestException() {
        super(ErrorCode.INVALID_REQUEST);
    }
}