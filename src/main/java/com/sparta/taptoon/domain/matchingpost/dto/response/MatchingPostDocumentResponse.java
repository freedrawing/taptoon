package com.sparta.taptoon.domain.matchingpost.dto.response;

import com.sparta.taptoon.domain.matchingpost.entity.document.MatchingPostDocument;
import com.sparta.taptoon.domain.matchingpost.enums.ArtistType;
import com.sparta.taptoon.domain.matchingpost.enums.WorkType;

import java.time.LocalDateTime;
import java.util.List;

public record MatchingPostDocumentResponse(
        Long id,
        Long authorId,
        ArtistType artistType,
        String title,
        WorkType workType,
        String description,
        Long viewCount,
        List<String> fileList, // 여기에는 이미지와 텍스트 파일 포함
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
                document.getFileImageUrlList(),
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }
}