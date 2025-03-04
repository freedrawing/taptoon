package com.sparta.taptoon.global.common.scheduler;

import com.sparta.taptoon.domain.image.service.AwsS3Service;
import com.sparta.taptoon.domain.matchingpost.entity.MatchingPostImage;
import com.sparta.taptoon.domain.matchingpost.repository.MatchingPostImageRepository;
import com.sparta.taptoon.domain.portfolio.entity.PortfolioFile;
import com.sparta.taptoon.domain.portfolio.repository.PortfolioFileRepository;
import com.sparta.taptoon.global.common.enums.Status;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ImageCleaner {
    private final MatchingPostImageRepository matchingPostImageRepository;
    private final PortfolioFileRepository portfolioFileRepository;
    private final AwsS3Service s3Service;

    // 매시간 실행 (1시간 이상 지난 PENDING -> DELETING)
    @Scheduled(cron = "0 0 * * * *") // 매시간 0분에 실행
    @Transactional
    public void updatePendingToDeleting() {
        LocalDateTime targetTime = LocalDateTime.now().minusHours(1);//생성된지 1시간 이상 지난 객체만 타게팅
        List<MatchingPostImage> orphanImages = matchingPostImageRepository.findByStatusAndCreatedAtBefore(
                Status.PENDING, targetTime
        );
        for (MatchingPostImage image : orphanImages) {
            image.changeStatusForDelete();
        }
        matchingPostImageRepository.saveAll(orphanImages);

        List<PortfolioFile> orphanPortfolioFiles = portfolioFileRepository.findByStatusAndCreatedAtBefore(
                Status.PENDING, targetTime
        );
        for (PortfolioFile file : orphanPortfolioFiles) {
            file.removeFile();
        }
        portfolioFileRepository.saveAll(orphanPortfolioFiles);
    }

    // 매일 자정 실행 (DELETING 상태 정리)
    @Scheduled(cron = "0 0 0 * * *") // 매일 00:00에 실행
    @Transactional
    public void cleanupDeletingImages() {
        LocalDateTime targetTime = LocalDateTime.now().minusHours(1);//생성된지 1시간 이상 지난 객체만 타게팅
        List<MatchingPostImage> orphanImages = matchingPostImageRepository.findByStatusAndCreatedAtBefore(
                Status.DELETING, targetTime
        );
        for (MatchingPostImage image : orphanImages) {
            s3Service.removeObject(image.getOriginalImageUrl());
            matchingPostImageRepository.deleteAllInBatch(orphanImages);
        }

        List<PortfolioFile> orphanPortfolioFiles = portfolioFileRepository.findByStatusAndCreatedAtBefore(
                Status.DELETING, targetTime
        );
        for (PortfolioFile file : orphanPortfolioFiles) {
            s3Service.removeObject(file.getFileUrl());
            portfolioFileRepository.deleteAllInBatch(orphanPortfolioFiles);
        }
    }
}
