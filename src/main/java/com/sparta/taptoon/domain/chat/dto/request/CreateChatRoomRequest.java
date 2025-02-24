package com.sparta.taptoon.domain.chat.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sparta.taptoon.domain.chat.entity.ChatRoom;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;

@Builder
public record CreateChatRoomRequest(
        @JsonProperty("memberIds")
        @NotNull(message = "채팅방에 초대할 유저를 한명 이상 입력해주세요")
        List<Long> memberIds
) {

    public static ChatRoom toEntity() {
        return ChatRoom.builder()
                .build();
    }
}
