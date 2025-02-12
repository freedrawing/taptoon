package com.sparta.taptoon.domain.chat.dto.request;

import com.sparta.taptoon.domain.chat.entity.ChatRoom;
import com.sparta.taptoon.domain.user.entity.User;

public record CreateChatRoomRequest(Long userId1, Long userId2) {
    public ChatRoom toEntity(User user1, User user2){
        return ChatRoom.builder()
                .user1(user1)
                .user2(user2)
                .build();
    }
}
