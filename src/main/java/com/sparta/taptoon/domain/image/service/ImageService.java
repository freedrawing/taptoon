package com.sparta.taptoon.domain.image.service;

import com.sparta.taptoon.domain.image.dto.response.ChatPresignedUrlResponse;
import com.sparta.taptoon.domain.image.dto.response.PresignedUrlResponse;

public interface ImageService {
    PresignedUrlResponse generatePresignedUrl(String folderPath, Long id, String fileType, String fileName);
    ChatPresignedUrlResponse generatePresignedUrl(String folderPath, String roomId, Long memberId, String fileName);
    void removeFileFromS3(String imageUrl);
}
