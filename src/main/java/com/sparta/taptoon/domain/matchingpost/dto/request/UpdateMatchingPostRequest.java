package com.sparta.taptoon.domain.matchingpost.dto.request;

// artistType와 업로드한 파일은 못 바꾸게
public record UpdateMatchingPostRequest(
        String title,
        String workType,
        String description
) {
}
