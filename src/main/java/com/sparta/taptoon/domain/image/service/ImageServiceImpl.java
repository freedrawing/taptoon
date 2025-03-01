package com.sparta.taptoon.domain.image.service;

import com.sparta.taptoon.domain.chat.entity.ChatImageMessage;
import com.sparta.taptoon.domain.chat.entity.ChatRoom;
import com.sparta.taptoon.domain.chat.repository.ChatImageMessageRepository;
import com.sparta.taptoon.domain.chat.repository.ChatRoomRepository;
import com.sparta.taptoon.domain.image.dto.response.PresignedUrlResponse;
import com.sparta.taptoon.domain.matchingpost.service.MatchingPostService;
import com.sparta.taptoon.domain.member.entity.Member;
import com.sparta.taptoon.domain.member.repository.MemberRepository;
import com.sparta.taptoon.domain.portfolio.entity.Portfolio;
import com.sparta.taptoon.domain.portfolio.entity.PortfolioImage;
import com.sparta.taptoon.domain.portfolio.repository.PortfolioImageRepository;
import com.sparta.taptoon.domain.portfolio.repository.PortfolioRepository;
import com.sparta.taptoon.global.common.enums.Status;
import com.sparta.taptoon.global.error.enums.ErrorCode;
import com.sparta.taptoon.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.sparta.taptoon.global.common.Constant.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private final AwsS3Service awsS3Service;
    private final MatchingPostService matchingPostService;
    private final PortfolioRepository portfolioRepository;
    private final PortfolioImageRepository portfolioImageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository;
    private final ChatImageMessageRepository chatImageMessageRepository;

    @Transactional // 이것도 나중에는 필요 없음
    @Override
    public PresignedUrlResponse generatePresignedUrl(String folderPath, Long id, String fileName) {
        String fileNameWithId = String.format("%s-%s", id, fileName);
        String thumbnailPath = String.format("%s/%s", folderPath, S3_THUMBNAIL_IMAGE_PATH.getStringConstant());
        String originalPath = String.format("%s/%s", folderPath, S3_ORIGINAL_IMAGE_PATH.getStringConstant());

        String presignedUrl = awsS3Service.generatePresignedUrl(originalPath, fileNameWithId);

        String thumbnailImageFullPath = awsS3Service.getFullUrl(thumbnailPath, fileNameWithId);
        String originalImageFullPath = awsS3Service.getFullUrl(originalPath, fileNameWithId);

        return folderPath.contains(S3_MATCHING_POST_IMAGE_PATH.getStringConstant())
                ? getMatchingPostPresignedUrl(id, thumbnailImageFullPath, originalImageFullPath, presignedUrl)
                : savePortfolioImage(id, originalImageFullPath, presignedUrl);
    }

    private PresignedUrlResponse getMatchingPostPresignedUrl(Long id, String thumbnailImageFullPath,
                                                             String originalImageFullPath, String presignedUrl) {

        Long matchingPostImageId = matchingPostService.generateEmptyMatchingPostImage(id, thumbnailImageFullPath, originalImageFullPath);
        return new PresignedUrlResponse(presignedUrl, matchingPostImageId);
    }

    private PresignedUrlResponse savePortfolioImage(Long id, String imageFullPath, String presignedUrl) {
        Portfolio portfolio = portfolioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PORTFOLIO_NOT_FOUND));
        PortfolioImage image = PortfolioImage.builder()
                .portfolio(portfolio)
                .fileUrl(imageFullPath)
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

        String directory = folderPath + "original/" + chatRoomId + ":" + memberId + "-";
        String imageFullPath = awsS3Service.getFullUrl(directory, fileName);
        String presignedUrl = awsS3Service.generatePresignedUrl(directory, fileName);

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
        return presignedUrl;
    }

    @Override
    public void removeImageFromS3(String url) {
        awsS3Service.removeObject(url);
    }
}