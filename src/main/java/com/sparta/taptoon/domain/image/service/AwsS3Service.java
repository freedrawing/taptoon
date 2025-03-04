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

/**
 * 순환참조가 발생해서 만들었습니다.
 * 이미지 삭제할 때 MatchingPostService에서 이미지를 삭제하기 위해서 ImageSerivce를 참조해야 하는데 참조하면 순환참조가 발생해서 만들었습니다.
 * 또한 현재 ImageService에 여러 도메인과 로직이 혼재돼 있어서, S3 관련된 로직은 여기에 넣는 게 더 좋아 보입니다.
 * 안 바꾸면 작업 진행이 안 돼서...
 * 만약 ImageServiceImpl를 그냥 놔두면 MatchingPostImageService를 따로 만들기에는 로직도 별로 없고, PortfolioImageService도 만들어줘야 해서...
 * 추후에 Chatting 관련 로직도 ImageServiceImpl에서 따로 분리하는 게 좋아보입니다.
 * 마음대로 바꿔서 미안합니당.
 */
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
        String key = url.substring(url.indexOf(".com/") + 5);
        if (key.contains("?")) {
            key = key.substring(0, key.indexOf("?"));
        }
        try {
            amazonS3.deleteObject(BUCKET, key);
            log.info("S3에서 객체 삭제 성공. key: {}", key);
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
        if (!path.endsWith("/")) {
            return path + "/";
        }
        return path.toLowerCase();
    }

}