package com.sparta.taptoon.global.exception.base;

import com.sparta.taptoon.global.exception.enums.ErrorCode;

public class EntityAlreadyExistsException extends BaseException {

    public EntityAlreadyExistsException(ErrorCode errorCode) {
        super(errorCode);
    }

    public EntityAlreadyExistsException() {
        super(ErrorCode.ENTITY_ALREADY_EXISTS);
    }

}
