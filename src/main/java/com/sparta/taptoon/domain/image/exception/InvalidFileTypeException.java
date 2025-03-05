package com.sparta.taptoon.domain.image.exception;

import com.sparta.taptoon.global.error.enums.ErrorCode;
import com.sparta.taptoon.global.error.exception.BaseException;

public class InvalidFileTypeException extends BaseException {

    public InvalidFileTypeException() {
        super(ErrorCode.INVALID_FILE_TYPE);
    }

    public InvalidFileTypeException(ErrorCode errorCode) {
        super(errorCode);
    }

    public InvalidFileTypeException(String message) {
        super(message, ErrorCode.INVALID_FILE_TYPE);
    }

    public InvalidFileTypeException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}
