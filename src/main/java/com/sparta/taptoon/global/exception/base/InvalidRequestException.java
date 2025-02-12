package com.sparta.taptoon.global.exception.base;

import com.sparta.taptoon.global.exception.enums.ErrorCode;

public class InvalidRequestException extends BaseException {

    public InvalidRequestException(ErrorCode errorCode) {
        super(errorCode);
    }

    public InvalidRequestException() {
        super(ErrorCode.INVALID_REQUEST);
    }
}