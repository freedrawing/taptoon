package com.sparta.taptoon.domain.chat.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record SendChatMessageRequest(
        @NotBlank(message = "메시지를 입력해주세요")
        @JsonProperty("message")
        String message

) {}
