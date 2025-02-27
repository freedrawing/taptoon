package com.sparta.taptoon.domain.matchingpost.dto.response;

import com.sparta.taptoon.domain.matchingpost.entity.MatchingPostImage;

public record MatchingPostImageResponse(
        Long id,
        Long matchingPostId,
        String imageUrl
) {

    public static MatchingPostImageResponse from(MatchingPostImage matchingPostImage) {
        return new MatchingPostImageResponse(
                matchingPostImage.getId(),
                matchingPostImage.getMatchingPost().getId(),
                matchingPostImage.getImageUrl()
        );
    }
}
