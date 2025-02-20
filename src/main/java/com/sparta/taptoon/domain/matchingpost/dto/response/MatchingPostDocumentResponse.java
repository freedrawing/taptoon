//package com.sparta.taptoon.domain.matchingpost.dto.response;
//
//import com.sparta.taptoon.domain.matchingpost.entity.document.MatchingPostDocument;
//import com.sparta.taptoon.domain.matchingpost.enums.ArtistType;
//import com.sparta.taptoon.domain.matchingpost.enums.WorkType;
//
//import java.time.Instant;
//import java.time.LocalDateTime;
//import java.time.ZoneId;
//import java.util.List;
//
//public record MatchingPostDocumentResponse(
//        Long id,
//        Long authorId,
//        ArtistType artistType,
//        String title,
//        WorkType workType,
//        String description,
//        Long viewCount,
//        Boolean isDeleted,
//        List<String> fileImageUrlList,
//        LocalDateTime createdAt,
//        LocalDateTime updatedAt
//) {
//    public static MatchingPostDocumentResponse from(MatchingPostDocument document) {
//        return new MatchingPostDocumentResponse(
//                document.getId(),
//                document.getAuthorId(),
//                document.getArtistType(),
//                document.getTitle(),
//                document.getWorkType(),
//                document.getDescription(),
//                document.getViewCount(),
//                document.getIsDeleted(),
//                document.getFileImageUrlList(),
//                toLocalDateTime(document.getCreatedAt()),
//                toLocalDateTime(document.getUpdatedAt())
//        );
//    }
//
//    private static LocalDateTime toLocalDateTime(Instant instant) {
//        return instant != null ? LocalDateTime.ofInstant(instant, ZoneId.systemDefault()) : null;
//    }
//}