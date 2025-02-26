package com.sparta.taptoon.domain.matchingpost.dto.response;

import com.sparta.taptoon.domain.matchingpost.entity.document.MatchingPostDocument;
import com.sparta.taptoon.domain.matchingpost.enums.ArtistType;
import com.sparta.taptoon.domain.matchingpost.enums.WorkType;

import java.time.LocalDateTime;

public record MatchingPostDocumentResponse(
        Long id,
        Long authorId,
        ArtistType artistType,
        String title,
        WorkType workType,
        String description,
        Long viewCount,
        String thumbnailImageUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static MatchingPostDocumentResponse from(MatchingPostDocument document) {
        return new MatchingPostDocumentResponse(
                document.getId(),
                document.getAuthorId(),
                document.getArtistType(),
                document.getTitle(),
                document.getWorkType(),
                document.getDescription(),
                document.getViewCount(),
                "https://github.com/user-attachments/assets/92f2c109-95ac-4a60-94da-0049b4a2992c",
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }
}