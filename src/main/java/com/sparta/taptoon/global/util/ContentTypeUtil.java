package com.sparta.taptoon.global.util;

import com.sparta.taptoon.domain.image.exception.InvalidFileTypeException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class    ContentTypeUtil {

    private static final Map<String, String> CONTENT_TYPE_MAP = Collections.unmodifiableMap(new HashMap<>() {{
        put("jpg", "image/jpeg");
        put("jpeg", "image/jpeg");
        put("png", "image/png");
        put("gif", "image/gif");

        put("txt", "text/plain");
        put("html", "text/html");
        put("htm", "text/html");
        put("csv", "text/csv");
        put("pdf", "application/pdf");
        put("doc", "application/msword");
        put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        put("md", "text/markdown");
    }});

    public static String getContentType(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            throw new InvalidFileTypeException();
        }

        // 확장자가 없을 때
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex == -1 || dotIndex == fileName.length() - 1) {
            throw new InvalidFileTypeException("지원하지 않는 파일 타입입니다.");
        }

        // 유효한 확장자가 아닐 때
        String extension = fileName.substring(dotIndex + 1).toLowerCase();
        String contentType = CONTENT_TYPE_MAP.getOrDefault(extension, null);
        if (contentType == null) {
            throw new InvalidFileTypeException("지원하지 않는 파일 타입입니다: " + extension);
        }
        return contentType;
    }
}