package com.sparta.taptoon.domain.image.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.sparta.taptoon.global.error.exception.AccessDeniedException;
import com.sparta.taptoon.global.util.ContentTypeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

import static com.sparta.taptoon.global.common.Constant.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AwsS3Service {

    private static final long EXPIRE_TIME = 1000 * 60 * 5; // 클라이언트 사이드에서 이미지 업로드 가능 제한 시간 5분

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.url}")
    private String BUCKET_URL;

    @Value("${cloud.aws.s3.bucket}")
    private String BUCKET;

    public String getFullUrl(String filePath, String fileName) {
        return BUCKET_URL + normalizePath(filePath) + fileName;
    }

    public String generatePresignedUrl(String filePath, String fileName) {
        String contentType = ContentTypeUtil.getContentType(fileName);
        String fullPath = normalizePath(filePath) + fileName;
        GeneratePresignedUrlRequest request = generatePresignedUrlRequest(fullPath, contentType);
        return amazonS3.generatePresignedUrl(request).toString();
    }

    public void removeObject(String url) {
        String key = url.substring(url.indexOf(COM_PATH) + 5);
        if (key.contains(PARAM_MARK)) {
            key = key.substring(0, key.indexOf("?"));
        }
        try {
            //원본 파일 삭제
            amazonS3.deleteObject(BUCKET, key);
            log.info("S3에서 객체 삭제 성공. key: {}", key);
            // 삭제하려는 파일이 이미지면 썸네일 파일 삭제
            if (isImageFile(key)) {
                String thumbnailKey = key.replace(ORIGINAL_FILE_PATH, THUMBNAIL_FILE_PATH);
                amazonS3.deleteObject(BUCKET, thumbnailKey);
                log.info("S3에서 썸네일 객체 삭제 성공. key: {}", thumbnailKey);
            }
        } catch (Exception e) {
            log.error("S3에서 객체 삭제 실패. key: {}", key, e);
            throw new AccessDeniedException("S3 연결에 실패했습니다.");
        }
    }

    private GeneratePresignedUrlRequest generatePresignedUrlRequest(String path, String contentType) {
        Date expiration = new Date();
        long expTimeMillis = expiration.getTime() + EXPIRE_TIME;
        expiration.setTime(expTimeMillis);

        return new GeneratePresignedUrlRequest(BUCKET, path)
                .withMethod(HttpMethod.PUT)
                .withExpiration(expiration)
                .withContentType(contentType);
    }

    private String normalizePath(String path) {
        if (!path.endsWith(SLASH_PATH)) {
            return path + SLASH_PATH;
        }
        return path.toLowerCase();
    }

    private boolean isImageFile(String key) {
        String lowerKey = key.toLowerCase();
        return lowerKey.endsWith(JPG) ||
                lowerKey.endsWith(JPEG) ||
                lowerKey.endsWith(PNG) ||
                lowerKey.endsWith(GIF);
    }
}