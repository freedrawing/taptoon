package com.sparta.taptoon.domain.file.dto.response;

public record PresignedUrlResponse(
        String uploadingImageUrl,
        Long imageEntityId
) {

}
