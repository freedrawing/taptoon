package com.sparta.taptoon.domain.matchingpost.dto.response;

import com.sparta.taptoon.domain.matchingpost.entity.MatchingPost;
import com.sparta.taptoon.domain.matchingpost.entity.document.MatchingPostDocument;
import com.sparta.taptoon.global.common.enums.Status;

import java.time.LocalDateTime;
import java.util.List;

public record MatchingPostResponse(
        Long matchingPostId,
        Long authorId,
        String authorName,
        String title,
        String description,
        String artistType,
        String workType,
        Long viewCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<MatchingPostImageResponse> imageList
) {

    public static MatchingPostResponse from(MatchingPost matchingPost) {
        return new MatchingPostResponse(
                matchingPost.getId(),
                matchingPost.getAuthor().getId(),
                matchingPost.getAuthor().getName(),
                matchingPost.getTitle(),
                matchingPost.getDescription(),
                matchingPost.getArtistType().name(),
                matchingPost.getWorkType().name(),
                matchingPost.getViewCount(),
                matchingPost.getCreatedAt(),
                matchingPost.getUpdatedAt(),
                matchingPost.getMatchingPostImages()
                        .stream()
                        .filter(matchingPostImage -> Status.isRegistered(matchingPostImage.getStatus()))
                        .map(MatchingPostImageResponse::from).toList()
        );
    }

    public static MatchingPostResponse from(MatchingPostDocument matchingPostDocument) {
        return new MatchingPostResponse(
                matchingPostDocument.getId(),
                matchingPostDocument.getAuthorId(),
                matchingPostDocument.getAuthorName(),
                matchingPostDocument.getTitle(),
                matchingPostDocument.getDescription(),
                matchingPostDocument.getArtistType().name(),
                matchingPostDocument.getWorkType().name(),
                matchingPostDocument.getViewCount(),
                matchingPostDocument.getCreatedAt(),
                matchingPostDocument.getUpdatedAt(),
                matchingPostDocument.getImageList()
        );
    }

}
