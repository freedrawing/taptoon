package com.sparta.taptoon.global.common;

import lombok.Getter;

@Getter
public class Constant {

    // About S3
    public static String S3_ORIGINAL_IMAGE_PATH = "original";
    public static String S3_THUMBNAIL_IMAGE_PATH = "thumbnail";
    public static String S3_MATCHING_POST_IMAGE_PATH = "matchingpost";
    public static String S3_PORTFOLIO_IMAGE_PATH = "portfolio";

    // FileType
    public static String FILE_TYPE = "file";
    public static String IMAGE_TYPE = "image";

    // Security Cors
    public static String LOCALHOST_SERVER = "http://localhost:8080";
    public static String LOCALHOST_CLIENT = "http://localhost:3000";
    public static String NAVER_CORS = "https://nid.naver.com";
    public static String S3_CLIENT = "http://taptoon-front.s3-website.ap-northeast-2.amazonaws.com";
    public static String TAPTOON = "https://taptoon.site";

    //Security whiteList
    public static String MATCHING_POST_URL = "/api/matching-posts/**";
    public static String COMMENTS_URL = "api/comments/**";
    public static String AUTH_URL = "/api/auth/**";
    public static String SWAGGER_DOCS_URL = "/v3/api-docs/**";
    public static String SWAGGER_UI_URL = "/swagger-ui/**";
    public static String SWAGGER_HTML_URL = "/swagger-ui.html";
}
