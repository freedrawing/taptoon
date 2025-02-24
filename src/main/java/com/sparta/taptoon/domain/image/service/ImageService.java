package com.sparta.taptoon.domain.image.service;

public interface ImageService {
    public String generatePresignedUrl(String folderPath, Long id, String fileName);
    public String generatePresignedUrl(String folderPath, Long roomId, Long memberId, String fileName);
}
