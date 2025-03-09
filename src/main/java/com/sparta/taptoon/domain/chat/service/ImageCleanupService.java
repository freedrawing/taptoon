package com.sparta.taptoon.domain.chat.service;

import com.sparta.taptoon.domain.chat.entity.ChatImageMessage;
import com.sparta.taptoon.domain.chat.repository.ChatImageMessageRepository;
import com.sparta.taptoon.domain.image.service.AwsS3Service;
import com.sparta.taptoon.global.common.enums.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageCleanupService {

    private final ChatImageMessageRepository chatImageMessageRepository;
    private final AwsS3Service awsS3Service;

    /**
     * 주기적으로 PENDING, DELETING, DELETED 상태의 이미지를 정리
     * - PENDING: 24시간 이상 지난 이미지 -> DELETING -> DELETED -> S3 삭제
     * - DELETING: S3 삭제 실패 케이스 -> S3 재삭제 -> DELETED
     * - DELETED: 소프트 삭제된 이미지 -> DB에서 완전 삭제
     */
    @Scheduled(fixedRate = 86400000) // 하루에 한 번 실행 (24시간 = 86,400,000ms)
    @Transactional
    public void cleanupImages() {
        // 1. 24시간 이상 지난 PENDING 이미지 정리
        LocalDateTime threshold = LocalDateTime.now().minusHours(24);
        List<ChatImageMessage> oldPendingImages = chatImageMessageRepository.findByStatusAndCreatedAtBefore(Status.PENDING, threshold);

        for (ChatImageMessage image : oldPendingImages) {
            try {
                image.updateStatus(Status.DELETING); // PENDING -> DELETING
                awsS3Service.removeObject(image.getOriginalImageUrl()); // S3 삭제
                image.updateStatus(Status.DELETED); // DELETING -> DELETED
                image.delete(); // 소프트 삭제
                chatImageMessageRepository.save(image);
                log.info("오래된 PENDING 이미지 {} DELETED로 업데이트", image.getId());
            } catch (Exception e) {
                log.error(" PENDING 이미지 정리 실패 {}: {}", image.getId(), e.getMessage());
                chatImageMessageRepository.save(image); // 실패 시 DELETING 상태로 남김
            }
        }

        // 2. DELETING 상태 이미지 정리 (S3에서 삭제 실패한 케이스)
        List<ChatImageMessage> deletingImages = chatImageMessageRepository.findByStatus(Status.DELETING);
        for (ChatImageMessage image : deletingImages) {
            try {
                awsS3Service.removeObject(image.getOriginalImageUrl()); // S3 재삭제 시도
                image.updateStatus(Status.DELETED); // DELETING -> DELETED
                image.delete(); // 소프트 삭제
                chatImageMessageRepository.save(image);
                log.info("DELETING 이미지 {} DELETED로 업데이트", image.getId());
            } catch (Exception e) {
                log.error("DELETING 이미지 정리 실패 {}: {}", image.getId(), e.getMessage());
                // 실패 시 DELETING 상태 유지
            }
        }

        // 3. DELETED 상태 이미지 완전 삭제
        List<ChatImageMessage> deletedImages = chatImageMessageRepository.findByStatus(Status.DELETED);
        for (ChatImageMessage image : deletedImages) {
            try {
                chatImageMessageRepository.delete(image); // 데이터베이스에서 완전 삭제
                log.info("DELETED 이미지 {} DB에서 완전히 삭제", image.getId());
            } catch (Exception e) {
                log.error("DELETED 이미지 완전 삭제 실패 {}: {}", image.getId(), e.getMessage());
                // 실패 시 다음 실행에서 재시도
            }
        }

        log.info("이미지 정리 완료: PENDING {}개 , DELETING {}개 ",
                oldPendingImages.size(), deletingImages.size());
    }
}
