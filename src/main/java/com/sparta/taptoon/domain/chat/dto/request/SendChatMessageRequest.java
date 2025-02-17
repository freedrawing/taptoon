package com.sparta.taptoon.domain.chat.dto.request;

import com.sparta.taptoon.domain.chat.entity.ChatMessage;
import com.sparta.taptoon.domain.chat.entity.ChatRoom;
import com.sparta.taptoon.domain.member.entity.Member;

public record SendChatMessageRequest(Long chatRoomId, Long senderId, String message) {

    public static ChatMessage toEntity(ChatRoom chatRoom, Member sender, String message) {
        return ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(sender)
                .message(message)
                .build();
    }
}
