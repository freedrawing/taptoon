package com.sparta.taptoon.domain.image.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.sparta.taptoon.domain.matchingpost.repository.MatchingPostImageRepository;
import com.sparta.taptoon.global.error.exception.AccessDeniedException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class ImageService {

    private static final long EXPIRE_TIME = 1000 * 60 * 5;//5분

    private final AmazonS3 amazonS3;
    private final MatchingPostImageRepository matchingPostImageRepository;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public String generatePresignedUrl(String folderPath, String fileName) {
        if (!folderPath.endsWith("/")) {
            folderPath += "/";
        }
        String contentType = getContentType(fileName);
        String directory = folderPath + "original/"+ fileName;
        Date expiration = new Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += EXPIRE_TIME;
        expiration.setTime(expTimeMillis);

        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(bucket, directory)
                        .withMethod(HttpMethod.PUT)
                        .withExpiration(expiration)
                        .withContentType(contentType);

        /**
         * 여기에 각 db에 저장하는 로직을 추가하면 됩니다.
         * 레디스를 이용해서 UUID,memberId, url 이 세개 저장. 나중에 UUID로 꺼내 쓰기. TTL 적용해서 알아서 지워지게. -> 이미지는 여러개 들어갈 수 있어야 함!
         * hash -> id, value, ..
         * UUID 하나만을 제공하는 API가 있어야 하나...? -> 클라이언트에서 UUID 를 생성 후에 호출한다고 가정한다면?
         */



        return amazonS3.generatePresignedUrl(generatePresignedUrlRequest).toString();
    }

    private String getContentType(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();

        return switch (extension) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            default -> throw new AccessDeniedException("지원하지 않는 이미지 타입입니다! " + extension);
        };
    }
}
