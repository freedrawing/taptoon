package com.sparta.taptoon.domain.matchingpost.dto.response;

import com.sparta.taptoon.domain.matchingpost.entity.MatchingPost;

import java.time.LocalDateTime;

public record MatchingPostResponse(
        Long matchingPostId,
        Long authorId,
        String title,
        String description,
        String artistType,
        String workType,
        String url, // image or file
        Long viewCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static MatchingPostResponse from(MatchingPost matchingPost) {
        return new MatchingPostResponse(
                matchingPost.getId(),
                matchingPost.getAuthor().getId(),
                matchingPost.getTitle(),
                matchingPost.getDescription(),
                matchingPost.getArtistType().name(),
                matchingPost.getWorkType().name(),
                matchingPost.getFileUrl(),
                matchingPost.getViewCount(),
                matchingPost.getCreatedAt(),
                matchingPost.getUpdatedAt()
        );
    }

}
