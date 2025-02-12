package com.sparta.taptoon.domain.chat.dto.response;

import com.sparta.taptoon.domain.chat.entity.ChatRoom;

public record ChatRoomResponse(Long roomId, Long user1Id, Long user2Id) {
    public static ChatRoomResponse from(ChatRoom chatRoom){
        return new ChatRoomResponse(
                chatRoom.getId(),
                chatRoom.getUser1().getId(),
                chatRoom.getUser2().getId()

        );
    }
}
