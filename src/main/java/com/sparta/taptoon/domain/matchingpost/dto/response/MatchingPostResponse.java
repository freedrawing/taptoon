package com.sparta.taptoon.domain.matchingpost.dto.response;

import com.sparta.taptoon.domain.matchingpost.entity.MatchingPost;

import java.time.LocalDateTime;
import java.util.List;

public record MatchingPostResponse(
        Long matchingPostId,
        Long authorId,
        String title,
        String description,
        String artistType,
        String workType,
        List<String> imageList,
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
                matchingPost.getFileUrlList(),
                matchingPost.getViewCount(),
                matchingPost.getCreatedAt(),
                matchingPost.getUpdatedAt()
        );
    }

}
