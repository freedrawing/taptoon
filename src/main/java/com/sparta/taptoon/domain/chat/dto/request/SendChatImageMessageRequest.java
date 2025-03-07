package com.sparta.taptoon.domain.chat.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record SendChatImageMessageRequest(
        @NotNull(message = "이미지 URL은 필수입니다.")
        @JsonProperty("image_url")
        String imageUrl

) {}
