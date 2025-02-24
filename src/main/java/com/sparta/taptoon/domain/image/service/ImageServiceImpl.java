package com.sparta.taptoon.domain.image.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.sparta.taptoon.domain.matchingpost.entity.MatchingPost;
import com.sparta.taptoon.domain.matchingpost.entity.MatchingPostImage;
import com.sparta.taptoon.domain.matchingpost.repository.MatchingPostImageRepository;
import com.sparta.taptoon.domain.matchingpost.repository.MatchingPostRepository;
import com.sparta.taptoon.domain.portfolio.entity.Portfolio;
import com.sparta.taptoon.domain.portfolio.entity.PortfolioImage;
import com.sparta.taptoon.domain.portfolio.repository.PortfolioImageRepository;
import com.sparta.taptoon.domain.portfolio.repository.PortfolioRepository;
import com.sparta.taptoon.global.common.enums.ImageStatus;
import com.sparta.taptoon.global.error.enums.ErrorCode;
import com.sparta.taptoon.global.error.exception.AccessDeniedException;
import com.sparta.taptoon.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService{

    private static final long EXPIRE_TIME = 1000 * 60 * 5;//5분

    private final AmazonS3 amazonS3;
    private final MatchingPostImageRepository matchingPostImageRepository;
    private final MatchingPostRepository matchingPostRepository;
    private final PortfolioRepository portfolioRepository;
    private final PortfolioImageRepository portfolioImageRepository;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Override
    public String generatePresignedUrl(String folderPath, Long id, String fileName) {//id 값을
        folderPath = normalizeFolderPath(folderPath);
        String contentType = getContentType(fileName);
        String directory = folderPath + id + "/original/"+ fileName;
        GeneratePresignedUrlRequest generatePresignedUrlRequest = generatePresignedUrlRequest(directory, contentType);

        String imageUrl = amazonS3.generatePresignedUrl(generatePresignedUrlRequest).toString();
        /*
         * 클라이언트에게 전달할 url 생성, 각 img 테이블에 url, status = PENDING 저장
         * 반드시 글쓰기 버튼 클릭(빈 객체 생성) 후에 진행해야 합니다!
         */
        switch (folderPath) {
            case "matchingpost":
                MatchingPost matchingPost = matchingPostRepository.findById(id)
                        .orElseThrow(() -> new NotFoundException(ErrorCode.MATCHING_POST_NOT_FOUND));
                MatchingPostImage matchingPostImage = MatchingPostImage.builder()
                        .matchingPost(matchingPost)
                        .imageUrl(imageUrl)
                        .status(ImageStatus.PENDING)
                        .build();
                matchingPostImageRepository.save(matchingPostImage);
                break;
            case "portfolio":
                Portfolio portfolio = portfolioRepository.findById(id)
                        .orElseThrow(() -> new NotFoundException(ErrorCode.PORTFOLIO_NOT_FOUND));
                PortfolioImage portfolioImage = PortfolioImage.builder()
                        .portfolio(portfolio)
                        .imageUrl(imageUrl)
                        .status(ImageStatus.PENDING)
                        .build();
                portfolioImageRepository.save(portfolioImage);
                break;
        }
        return imageUrl;
    }

    @Override
    public String generatePresignedUrl(String folderPath, Long roomId, Long memberId, String fileName) {
        folderPath = normalizeFolderPath(folderPath);
        String contentType = getContentType(fileName);
        String directory = folderPath + roomId + "/" + memberId + "/original/"+ fileName;
        GeneratePresignedUrlRequest generatePresignedUrlRequest = generatePresignedUrlRequest(directory, contentType);

        String imageUrl = amazonS3.generatePresignedUrl(generatePresignedUrlRequest).toString();
        /*
         * 여기에 채팅 임시 저장 해야합니다
         */
        return imageUrl;
    }

    private GeneratePresignedUrlRequest generatePresignedUrlRequest(String directory, String contentType) {
        Date expiration = new Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += EXPIRE_TIME;
        expiration.setTime(expTimeMillis);

        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(bucket, directory)
                        .withMethod(HttpMethod.PUT)
                        .withExpiration(expiration)
                        .withContentType(contentType);
        return generatePresignedUrlRequest;
    }

    private static String normalizeFolderPath(String folderPath) {
        if (!folderPath.endsWith("/")) {
            folderPath += "/";
        }
        folderPath = folderPath.toLowerCase();
        return folderPath;
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
