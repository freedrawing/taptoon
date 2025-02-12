package com.sparta.taptoon.domain.matchingpost.dto.response;

import com.sparta.taptoon.domain.matchingpost.entity.MatchingPost;

public record MatchingPostResponse(
        Long matchingPostId,
        String title,
        String description,
        String artistType,
        String workType,
        String url, // image or file
        Long viewCount
) {

    public static MatchingPostResponse from(MatchingPost matchingPost) {
        return new MatchingPostResponse(
                matchingPost.getId(),
                matchingPost.getTitle(),
                matchingPost.getDescription(),
                matchingPost.getArtistType().name(),
                matchingPost.getWorkType().name(),
                matchingPost.getFileUrl(),
                matchingPost.getViewCount()
        );
    }
}
