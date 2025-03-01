package com.sparta.taptoon.domain.matchingpost.service;

import com.sparta.taptoon.domain.image.service.AwsS3Service;
import com.sparta.taptoon.domain.matchingpost.entity.MatchingPostImage;
import com.sparta.taptoon.domain.matchingpost.repository.MatchingPostImageRepository;
import com.sparta.taptoon.domain.matchingpost.repository.MatchingPostRepository;
import com.sparta.taptoon.global.common.enums.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * `MatchingPost`와 `MatchingPostImage` 안 쓰는 이미지 정리
 * `PENDING` 상태는 12시간마다 `DELETING` 상태로 변경하고,
 * `DELETING` 상태는 시간마다 S3와 DB에서 제거
 *  이때 `MatchingPost`도 `DELETING`의 경우 아무런 의미도 없는 데이터이므로 Hard Deletion을 하자.
 */
@Component
@RequiredArgsConstructor
public class MatchingPostScheduler {

    private final MatchingPostRepository matchingPostRepository;
    private final MatchingPostImageRepository matchingPostImageRepository;
    private final AwsS3Service awsS3Service;

//    @Scheduled(fixedRate = 3600000) // 1시간마다 실행
//    public void cleanDeletingImages() {
//        matchingPostImageRepository.deleteByStatus(Status.DELETING);
//    }
//
//    @Scheduled(cron = "0 0 * * * ?") // 매 시간 0분에 실행 (PENDING 상태 체크)
//    public void updatePendingToDeleting() {
//        LocalDateTime threshold = LocalDateTime.now().minusHours(2); // 2시간 이상 PENDING이면 DELETING으로
//        List<MatchingPostImage> pendingImages = matchingPostImageRepository.findByStatus(Status.PENDING);
//        for (MatchingPostImage image : pendingImages) {
//            if (image.getCreatedAt().isBefore(threshold)) {
//                image.setStatus(Status.DELETING);
//                matchingPostImageRepository.save(image);
//            }
//        }
//    }
}
