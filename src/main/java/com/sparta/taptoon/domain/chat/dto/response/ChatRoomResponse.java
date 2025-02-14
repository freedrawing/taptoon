package com.sparta.taptoon.domain.chat.dto.response;

import com.sparta.taptoon.domain.chat.entity.ChatRoom;
import lombok.Builder;

@Builder
public record ChatRoomResponse(Long roomId, Long member1Id, Long member2Id) {
    public static ChatRoomResponse from(ChatRoom chatRoom) {
        return ChatRoomResponse.builder()
                .roomId(chatRoom.getId())
                .member1Id(chatRoom.getMember1().getId())
                .member2Id(chatRoom.getMember2().getId())
                .build();
    }
}
