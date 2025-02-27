package com.sparta.taptoon.domain.image.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.sparta.taptoon.domain.chat.entity.ChatImageMessage;
import com.sparta.taptoon.domain.chat.entity.ChatRoom;
import com.sparta.taptoon.domain.chat.repository.ChatImageMessageRepository;
import com.sparta.taptoon.domain.chat.repository.ChatRoomRepository;
import com.sparta.taptoon.domain.image.dto.response.PresignedUrlResponse;
import com.sparta.taptoon.domain.matchingpost.entity.MatchingPost;
import com.sparta.taptoon.domain.matchingpost.entity.MatchingPostImage;
import com.sparta.taptoon.domain.matchingpost.repository.MatchingPostImageRepository;
import com.sparta.taptoon.domain.matchingpost.repository.MatchingPostRepository;
import com.sparta.taptoon.domain.member.entity.Member;
import com.sparta.taptoon.domain.member.repository.MemberRepository;
import com.sparta.taptoon.domain.portfolio.entity.Portfolio;
import com.sparta.taptoon.domain.portfolio.entity.PortfolioImage;
import com.sparta.taptoon.domain.portfolio.repository.PortfolioImageRepository;
import com.sparta.taptoon.domain.portfolio.repository.PortfolioRepository;
import com.sparta.taptoon.global.common.enums.Status;
import com.sparta.taptoon.global.error.enums.ErrorCode;
import com.sparta.taptoon.global.error.exception.AccessDeniedException;
import com.sparta.taptoon.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private static final long EXPIRE_TIME = 1000 * 60 * 5;//5분

    private final AmazonS3 amazonS3;
    private final MatchingPostImageRepository matchingPostImageRepository;
    private final MatchingPostRepository matchingPostRepository;
    private final PortfolioRepository portfolioRepository;
    private final PortfolioImageRepository portfolioImageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository;
    private final ChatImageMessageRepository chatImageMessageRepository;

    private final String MATCHING_POST = "matchingpost"; // 여기 있는 것도 따로 CONSTANT 관리하는 걸로 빼면 좋을 듯
    private final String PORTFOLIO = "portfolio";

    @Value("${cloud.aws.s3.url}")
    private final String BUCKET_URL;

    @Value("${cloud.aws.s3.bucket}")
    private String BUCKET;

    @Transactional
    @Override
    public PresignedUrlResponse generatePresignedUrl(String folderPath, Long id, String fileName) {//id 값을
        folderPath = normalizeFolderPath(folderPath);
        String contentType = getContentType(fileName);
        String directory = folderPath + "original/" + id + "-" + fileName;
        String imageFullPath = BUCKET_URL + directory;

        GeneratePresignedUrlRequest generatePresignedUrlRequest = generatePresignedUrlRequest(directory, contentType);
        String presignedUrl = amazonS3.generatePresignedUrl(generatePresignedUrlRequest).toString();
        /*
         * 클라이언트에게 전달할 url 생성, 각 img 테이블에 url, status = PENDING 저장
         * 반드시 글쓰기 버튼 클릭(빈 객체 생성) 후에 진행해야 합니다!
         */
        return folderPath.contains(MATCHING_POST)
                ? saveMatchingPostImage(id, imageFullPath, presignedUrl)
                : savePortfolioImage(id, imageFullPath, presignedUrl);
    }

    private PresignedUrlResponse saveMatchingPostImage(Long id, String imageFullPath, String presignedUrl) {
        MatchingPost matchingPost = matchingPostRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MATCHING_POST_NOT_FOUND));

        MatchingPostImage image = MatchingPostImage.builder()
                .matchingPost(matchingPost)
                .imageUrl(imageFullPath)
                .status(Status.PENDING)
                .build();

        MatchingPostImage savedImage = matchingPostImageRepository.save(image);
        return new PresignedUrlResponse(presignedUrl, savedImage.getId());
    }

    private PresignedUrlResponse savePortfolioImage(Long id, String imageFullPath, String presignedUrl) {
        Portfolio portfolio = portfolioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PORTFOLIO_NOT_FOUND));

        PortfolioImage image = PortfolioImage.builder()
                .portfolio(portfolio)
                .imageUrl(imageFullPath)
                .status(Status.PENDING)
                .build();

        PortfolioImage savedImage = portfolioImageRepository.save(image);
        return new PresignedUrlResponse(presignedUrl, savedImage.getId());
    }

    @Override
    @Transactional
    public String generatePresignedUrl(String folderPath, Long chatRoomId, Long memberId, String fileName) {
        log.info("generatePresignedUrl 호출 - folderPath: {}, roomId: {}, memberId: {}, fileName: {}",
                folderPath, chatRoomId, memberId, fileName);
        folderPath = normalizeFolderPath(folderPath);
        String contentType = getContentType(fileName);

        String directory = folderPath + "original/" + chatRoomId + ":" + memberId + "-" + fileName;
        String imageFullPath = BUCKET_URL + directory;
        GeneratePresignedUrlRequest generatePresignedUrlRequest = generatePresignedUrlRequest(directory, contentType);

        String imageUrl = amazonS3.generatePresignedUrl(generatePresignedUrlRequest).toString();

        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.CHAT_ROOM_NOT_FOUND));
        Member sender = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.CHAT_MEMBER_NOT_FOUND));

        ChatImageMessage imageMessage = ChatImageMessage.builder()
                .chatRoom(chatRoom)
                .sender(sender)
                .imageUrl(imageFullPath)
                .unreadCount(0)
                .status(Status.PENDING)
                .build();

        if ("chat/".equals(folderPath)) {
            try {
                ChatImageMessage savedMessage = chatImageMessageRepository.saveAndFlush(imageMessage);
                log.info("ChatImageMessage 저장 완료 - ID: {}, URL: {}", savedMessage.getId(), savedMessage.getImageUrl());
            } catch (Exception e) {
                log.error("채팅 이미지 메시지 저장 실패 - roomId: {}, memberId: {}, 에러: {}", chatRoomId, memberId, e.getMessage(), e);
                throw e;
            }
        }
        return imageUrl;
    }

    @Override
    public void removeImageFromS3(String url) {
        String key = url.substring(url.indexOf(".com/")+5);
        if (key.contains("?")) {
            key = key.substring(0, key.indexOf("?"));
        }
        try{
            amazonS3.deleteObject(BUCKET,key);
            log.info("S3에서 이미지 삭제 성공. key : {}",key);
        } catch (Exception e) {
            log.info("S3에서 이미지 삭제 실패. key : {}",key);
            throw new AccessDeniedException(ErrorCode.FAIL_CONNECT_TO_S3);
        }
    }

    private GeneratePresignedUrlRequest generatePresignedUrlRequest(String directory, String contentType) {
        Date expiration = new Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += EXPIRE_TIME;
        expiration.setTime(expTimeMillis);

        return new GeneratePresignedUrlRequest(BUCKET, directory)
                .withMethod(HttpMethod.PUT)
                .withExpiration(expiration)
                .withContentType(contentType);
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
