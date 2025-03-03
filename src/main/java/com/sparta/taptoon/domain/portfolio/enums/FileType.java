package com.sparta.taptoon.domain.portfolio.enums;

import com.sparta.taptoon.global.error.exception.InvalidRequestException;

import java.util.Arrays;

import static com.sparta.taptoon.global.error.enums.ErrorCode.PORTFOLIO_INVALID_FILE_TYPE;

public enum FileType {
    FILE,
    IMAGE,

    ;

    public static FileType of(String type) {
        return Arrays.stream(values())
                .filter(fileType -> fileType.name().equalsIgnoreCase(type))
                .findFirst()
                .orElseThrow(() -> new InvalidRequestException(PORTFOLIO_INVALID_FILE_TYPE));
    }

    public static boolean isImageType(String type) {
        return of(type) == FileType.IMAGE;
    }
}
