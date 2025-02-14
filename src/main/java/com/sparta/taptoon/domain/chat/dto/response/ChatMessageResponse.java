package com.sparta.taptoon.domain.chat.dto.response;

import com.sparta.taptoon.domain.chat.entity.ChatMessage;

public record ChatMessageResponse(
        Long id,
        Long chatRoomId,
        Long senderId,
        String message,
        Boolean isRead
) {
    public static ChatMessageResponse from(ChatMessage entity) {
        return new ChatMessageResponse(
                entity.getId(),
                entity.getChatRoom().getId(),
                entity.getSender().getId(),
                entity.getMessage(),
                entity.getIsRead()
        );
    }
}
