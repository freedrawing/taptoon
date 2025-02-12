package com.sparta.taptoon.global.error.exception;

import com.sparta.taptoon.global.error.enums.ErrorCode;

public class NotFoundException extends BaseException {

    public NotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    public NotFoundException() {
        super(ErrorCode.NOT_FOUND);
    }
}
