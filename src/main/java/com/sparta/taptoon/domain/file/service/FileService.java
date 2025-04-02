package com.sparta.taptoon.domain.file.service;

import com.sparta.taptoon.domain.file.dto.response.ChatPresignedUrlResponse;
import com.sparta.taptoon.domain.file.dto.response.PresignedUrlResponse;

public interface FileService {
    PresignedUrlResponse generatePresignedUrl(String folderPath, Long id, String fileType, String fileName);
    ChatPresignedUrlResponse generatePresignedUrl(String folderPath, String roomId, Long memberId, String fileName);
    void removeFileFromS3(String imageUrl);
}
