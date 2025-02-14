package com.sparta.taptoon.domain.matchingpost.enums;

import com.sparta.taptoon.global.error.exception.InvalidRequestException;

import java.util.Arrays;
import java.util.Random;

import static com.sparta.taptoon.global.error.enums.ErrorCode.INVALID_ARTIEST_TYPE;

public enum ArtistType {
    ILLUSTRATOR, WRITER

    ;

    // 예외 처리용
    public static ArtistType of(String type) {
        return Arrays.stream(values())
                .filter(elem -> elem.name().equalsIgnoreCase(type))
                .findFirst()
                .orElseThrow(() -> new InvalidRequestException(INVALID_ARTIEST_TYPE));
    }

    // QueryDSL 검색용
    public static ArtistType fromString(String type) {
        return Arrays.stream(values())
                .filter(elem -> elem.name().equalsIgnoreCase(type))
                .findFirst()
                .orElse(null); // 일치하는 값이 없으면 null 반환
    }

    public static ArtistType random() {
        ArtistType[] types = values();
        return types[new Random().nextInt(values().length)];
    }


}
