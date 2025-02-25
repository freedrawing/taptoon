package com.sparta.taptoon.domain.image.dto.response;

public record PresignedUrlResponse(
        String uploadingImageUrl,
        Long imageEntityId
) {

}
