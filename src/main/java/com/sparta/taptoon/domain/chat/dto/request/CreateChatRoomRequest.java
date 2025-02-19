package com.sparta.taptoon.domain.chat.dto.request;

import com.sparta.taptoon.domain.chat.entity.ChatRoom;
import lombok.Builder;

import java.util.List;

@Builder
public record CreateChatRoomRequest(List<Long> memberIds) {

    public static ChatRoom toEntity() {
        return ChatRoom.builder()
                .build();
    }
}
