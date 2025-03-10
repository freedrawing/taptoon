package com.sparta.taptoon.domain.image.dto.response;

public record ChatPresignedUrlResponse(
        String uploadingImageUrl,
        String imageEntityId
) {}
