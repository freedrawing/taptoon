package com.sparta.taptoon.domain.chat.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public record SendChatImageMessageRequest(
        @NotNull(message = "이미지 메시지 ID는 필수입니다.")
        @JsonProperty("imageMessageId")
        Long imageMessageId
) {}
