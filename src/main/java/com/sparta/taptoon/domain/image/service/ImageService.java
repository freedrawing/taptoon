package com.sparta.taptoon.domain.image.service;

import com.sparta.taptoon.domain.image.dto.response.PresignedUrlResponse;

public interface ImageService {
    PresignedUrlResponse generatePresignedUrl(String folderPath, Long id, String fileName);
    String generatePresignedUrl(String folderPath, Long roomId, Long memberId, String fileName);
    void removeImageFromS3(String imageUrl);
}
