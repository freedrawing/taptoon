package com.sparta.taptoon.domain.matchingpost.dto.response;

public record MatchingPostImageResponse(
        Long id,
        Long matchingPostId,
        String imgUrl
) {
}
