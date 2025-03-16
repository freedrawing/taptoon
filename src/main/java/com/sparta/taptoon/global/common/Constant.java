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
    public static String TEST_SEVER_IP = "http://3.36.94.187";
    public static String TEST_SERVER_WITH_DOMAIN_ADDRESS = "http://tpi.taptoon.site";

    //Security whiteList
    public static String MATCHING_POST_URL = "/api/matching-posts/**";
    public static String COMMENTS_URL = "/api/comments/**";
    public static String PORTFOLIO_URL = "/api/portfolios/**";
    public static String AUTH_URL = "/api/auth/**";
    public static String SWAGGER_DOCS_URL = "/v3/api-docs/**";
    public static String SWAGGER_UI_URL = "/swagger-ui/**";
    public static String SWAGGER_HTML_URL = "/swagger-ui.html";
    public static String CHATTING_NOTIFICATION =  "/notifications/**";
    public static String CHATTING_WEBSOCKET =  "/ws/chat/**";
    public static String HEALTH =  "/health";

    //s3
    public static String PARAM_MARK = "?";
    public static String ORIGINAL_FILE_PATH = "/original/";
    public static String THUMBNAIL_FILE_PATH = "/thumbnail/";
    public static String SLASH_PATH = "/";
    public static String COM_PATH = ".com/";
    public static String JPG = ".jpg";
    public static String JPEG = ".jpeg";
    public static String PNG = ".png";
    public static String GIF = ".gif";
}
