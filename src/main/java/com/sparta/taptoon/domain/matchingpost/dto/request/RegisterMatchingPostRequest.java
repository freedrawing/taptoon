package com.sparta.taptoon.domain.matchingpost.dto.request;

import java.util.List;

public record RegisterMatchingPostRequest(
        String title,
        String description,
        String artistType,
        String workType,
        List<Long> matchingPostImageIds // 이미지 PENDING -> REGISTERED
) {
}
