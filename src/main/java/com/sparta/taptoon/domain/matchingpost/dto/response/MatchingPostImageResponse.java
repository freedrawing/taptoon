package com.sparta.taptoon.domain.matchingpost.dto.response;

import com.sparta.taptoon.domain.matchingpost.entity.MatchingPostImage;

public record MatchingPostImageResponse(
        Long id,
        Long matchingPostId,
        String fileName,
        String thumbnailImageUrl,
        String originalImageUrl
) {

    public static MatchingPostImageResponse from(MatchingPostImage matchingPostImage) {
        return new MatchingPostImageResponse(
                matchingPostImage.getId(),
                matchingPostImage.getMatchingPost().getId(),
                matchingPostImage.getFileName(),
                matchingPostImage.getThumbnailImageUrl(),
                matchingPostImage.getOriginalImageUrl()
        );
    }
}
