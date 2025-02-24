package com.sparta.taptoon.domain.matchingpost.dto.response;

import com.sparta.taptoon.domain.matchingpost.entity.document.MatchingPostDocument;

import java.util.List;

// 커서기반 조회할 때 사용되는 plate class
public record MatchingPostCursorResponse(
        List<MatchingPostDocumentResponse> content,
        Long lastId, // 마지막으로 읽은 문서ID
        Long lastViewCount // 마지막으로 읽은 viewCount
) {
}
