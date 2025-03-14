package com.sparta.taptoon.domain.matchingpost.enums;

import com.sparta.taptoon.global.error.exception.InvalidRequestException;

import java.util.Arrays;
import java.util.Random;

import static com.sparta.taptoon.global.error.enums.ErrorCode.INVALID_WORK_TYPE;

public enum WorkType {
    ONLINE,
    OFFLINE,
    HYBRID
    ;

    // 예외 처리용
    public static WorkType of(String type) {
        return Arrays.stream(values())
                .filter(elem -> elem.name().equalsIgnoreCase(type))
                .findFirst()
                .orElseThrow(() -> new InvalidRequestException(INVALID_WORK_TYPE));
    }

    // 검색용
    public static WorkType fromString(String type) {
        return Arrays.stream(values())
                .filter(elem -> elem.name().equalsIgnoreCase(type))
                .findFirst()
                .orElse(null);
    }

    public static WorkType random() {
        WorkType[] types = values();
        return types[new Random().nextInt(values().length)];
    }
}
