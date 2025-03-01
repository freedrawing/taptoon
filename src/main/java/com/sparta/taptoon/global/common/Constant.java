package com.sparta.taptoon.global.common;

import lombok.Getter;

@Getter
public enum Constant {

    // About S3
    S3_ORIGINAL_IMAGE_PATH("original"),
    S3_THUMBNAIL_IMAGE_PATH("thumbnail"),
    S3_MATCHING_POST_IMAGE_PATH("matchingpost"),
    S3_PORTFOLIO_IMAGE_PATH("portfolio"),

    ;

    Constant(String stringConstant) {
        this.stringConstant = stringConstant;
    }

    private final String stringConstant;
}
