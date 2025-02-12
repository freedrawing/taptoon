package com.sparta.taptoon.domain.matchingpost.enums;

import com.sparta.taptoon.global.error.exception.InvalidRequestException;

import java.util.Arrays;

import static com.sparta.taptoon.global.error.enums.ErrorCode.INVALID_ARTIEST_TYPE;

public enum ArtistType {
    ILLUSTRATOR, WRITER

    ;

    public static ArtistType of(String type) {
        return Arrays.stream(values())
                .filter(elem -> elem.name().equalsIgnoreCase(type))
                .findFirst()
                .orElseThrow(() -> new InvalidRequestException(INVALID_ARTIEST_TYPE));
    }

}
