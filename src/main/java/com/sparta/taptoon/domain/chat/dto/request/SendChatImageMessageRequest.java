package com.sparta.taptoon.domain.chat.dto.request;

import java.util.List;

public record SendChatImageMessageRequest(List<String> imageIds) {
    public SendChatImageMessageRequest {
        if (imageIds == null || imageIds.size() > 5) {
            throw new IllegalArgumentException("이미지 ID는 null이거나 5개를 초과할 수 없습니다.");
        }
    }
}
