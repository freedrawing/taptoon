package com.sparta.taptoon.global.error.exception;

import com.sparta.taptoon.global.error.enums.ErrorCode;

public class EntityAlreadyExistsException extends BaseException {

    public EntityAlreadyExistsException(ErrorCode errorCode) {
        super(errorCode);
    }

    public EntityAlreadyExistsException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }

    public EntityAlreadyExistsException() {
        super(ErrorCode.ENTITY_ALREADY_EXISTS);
    }

}
