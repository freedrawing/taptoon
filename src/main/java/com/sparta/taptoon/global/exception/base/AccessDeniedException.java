package com.sparta.taptoon.global.exception.base;

import com.sparta.taptoon.global.exception.enums.ErrorCode;

public class AccessDeniedException extends BaseException {

    public AccessDeniedException(ErrorCode errorCode) {
        super(errorCode);
    }

    public AccessDeniedException() {
        super(ErrorCode.ACCESS_DENIED);
    }

    public AccessDeniedException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }

    public AccessDeniedException(String message) {
        super(message, ErrorCode.ACCESS_DENIED);
    }
}
