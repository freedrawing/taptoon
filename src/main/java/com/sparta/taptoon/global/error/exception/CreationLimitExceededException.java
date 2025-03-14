package com.sparta.taptoon.global.error.exception;

import com.sparta.taptoon.global.error.enums.ErrorCode;

public class CreationLimitExceededException extends BaseException {

    public CreationLimitExceededException(ErrorCode errorCode) {
        super(errorCode);
    }

    public CreationLimitExceededException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }

}
