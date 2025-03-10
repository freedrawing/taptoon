package com.sparta.taptoon.domain.image.service;

import com.sparta.taptoon.domain.chat.entity.ChatImageMessage;
import com.sparta.taptoon.domain.chat.repository.ChatImageMessageRepository;
import com.sparta.taptoon.domain.chat.repository.ChatRoomRepository;
import com.sparta.taptoon.domain.image.dto.response.ChatPresignedUrlResponse;
import com.sparta.taptoon.domain.image.dto.response.PresignedUrlResponse;
import com.sparta.taptoon.domain.matchingpost.service.MatchingPostService;
import com.sparta.taptoon.domain.member.repository.MemberRepository;
import com.sparta.taptoon.domain.portfolio.enums.FileType;
import com.sparta.taptoon.domain.portfolio.service.PortfolioService;
import com.sparta.taptoon.global.common.enums.Status;
import com.sparta.taptoon.global.error.enums.ErrorCode;
import com.sparta.taptoon.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.sparta.taptoon.global.common.Constant.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private final AwsS3Service awsS3Service;
    private final MatchingPostService matchingPostService;
    private final PortfolioService portfolioService;
    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository;
    private final ChatImageMessageRepository chatImageMessageRepository;

    @Override
    public PresignedUrlResponse generatePresignedUrl(String folderPath, Long id, String fileType, String fileName) {
        String fileNameWithId = String.format("%s-%s", id, fileName);
        String thumbnailPath = String.format("%s/%s", folderPath, S3_THUMBNAIL_IMAGE_PATH);
        String originalPath = String.format("%s/%s", folderPath, S3_ORIGINAL_IMAGE_PATH);

        String presignedUrl = awsS3Service.generatePresignedUrl(originalPath, fileNameWithId);

        String thumbnailImageFullPath = awsS3Service.getFullUrl(thumbnailPath, fileNameWithId);
        String originalImageFullPath = awsS3Service.getFullUrl(originalPath, fileNameWithId);

        return folderPath.contains(S3_MATCHING_POST_IMAGE_PATH)
                ? getMatchingPostPresignedUrl(id, fileName, thumbnailImageFullPath, originalImageFullPath, presignedUrl)
                : getPortfolioFilePresignedUrl(id, fileName, fileType, thumbnailImageFullPath, originalImageFullPath, presignedUrl);
    }

    // `MatchingPostImage` 저장용
    private PresignedUrlResponse getMatchingPostPresignedUrl(Long id,
                                                             String fileName,
                                                             String thumbnailImageFullPath,
                                                             String originalImageFullPath,
                                                             String presignedUrl) {

        Long matchingPostImageId =
                matchingPostService.generateEmptyMatchingPostImage(id, fileName, thumbnailImageFullPath, originalImageFullPath);
        return new PresignedUrlResponse(presignedUrl, matchingPostImageId);
    }

    // `PortfolioFile` 저장용
    private PresignedUrlResponse getPortfolioFilePresignedUrl(Long id,
                                                              String fileName,
                                                              String fileType,
                                                              String thumbnailImageFullPath,
                                                              String originalFileFullPath,
                                                              String presignedUrl) {

        // file 타입이면 thumbnailUrl 필요 없음. 있어서도 안 됨. Entity에는 null 저장
        String filteredThumbnailUrl = FileType.isImageType(fileType) ? thumbnailImageFullPath : null;

        Long portfolioFileId =
                portfolioService.generateEmptyPortfolioFile(id, fileName, fileType, filteredThumbnailUrl, originalFileFullPath);
        return new PresignedUrlResponse(presignedUrl, portfolioFileId);
    }

    // 여기는 AwsS3Service로 로직을 변경해서 잘 안 될 수도 있을 듯. 진영님한테 확인해달라고 해야할 듯.
    // Chat용
    @Override
    public ChatPresignedUrlResponse generatePresignedUrl(String folderPath, String chatRoomId, Long memberId, String fileName) {
        String fileNameWithId = String.format("%s-%s", chatRoomId, fileName);
        String thumbnailPath = String.format("%s/%s", folderPath, S3_THUMBNAIL_IMAGE_PATH);
        String originalPath = String.format("%s/%s", folderPath, S3_ORIGINAL_IMAGE_PATH);

        String presignedUrl = awsS3Service.generatePresignedUrl(originalPath, fileNameWithId);
        String thumbnailImageFullPath = awsS3Service.getFullUrl(thumbnailPath, fileNameWithId);
        String originalImageFullPath = awsS3Service.getFullUrl(originalPath, fileNameWithId);

        return getChatPresignedUrl(chatRoomId, memberId, fileName,thumbnailImageFullPath, originalImageFullPath, presignedUrl);
    }

    private ChatPresignedUrlResponse getChatPresignedUrl(String chatRoomId, Long memberId, String fileName,
                                                         String thumbnailImageFullPath, String originalImageFullPath,
                                                         String presignedUrl) {
        if (!chatRoomRepository.existsById(chatRoomId)) {
            throw new NotFoundException(ErrorCode.CHAT_ROOM_NOT_FOUND);
        }
        if (!memberRepository.existsById(memberId)) {
            throw new NotFoundException(ErrorCode.CHAT_MEMBER_NOT_FOUND);
        }

        ChatImageMessage imageMessage = ChatImageMessage.builder()
                .chatRoomId(chatRoomId)
                .senderId(memberId)
                .thumbnailImageUrl(thumbnailImageFullPath)
                .originalImageUrl(originalImageFullPath)
                .unreadCount(0)
                .status(Status.PENDING)
                .build();

        ChatImageMessage savedMessage = chatImageMessageRepository.save(imageMessage);
        log.info("ChatImageMessage 저장 완료 - ID: {}, Thumbnail URL: {}, Original URL: {}",
                savedMessage.getId(), savedMessage.getThumbnailImageUrl(), savedMessage.getOriginalImageUrl());

        return new ChatPresignedUrlResponse(presignedUrl, savedMessage.getId());
    }

    @Override
    public void removeFileFromS3(String url) {
        awsS3Service.removeObject(url);
    }
}