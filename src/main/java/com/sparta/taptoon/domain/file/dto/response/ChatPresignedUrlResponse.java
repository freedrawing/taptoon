package com.sparta.taptoon.domain.file.dto.response;

public record ChatPresignedUrlResponse(
        String uploadingImageUrl,
        String imageEntityId
) {}
