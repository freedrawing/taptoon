package com.sparta.taptoon.domain.chat.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;

import java.util.List;

@Builder
public record CreateChatRoomRequest(
        @JsonProperty("member_ids")
        @NotEmpty(message = "채팅방에 초대할 유저를 한 명 이상 입력해주세요")
        List<Long> memberIds
) {}
