package com.sparta.taptoon.domain.chat.dto.response;

import com.sparta.taptoon.domain.chat.entity.ChatMessage;
import lombok.Builder;

@Builder
public record ChatMessageResponse(
        Long id,
        Long chatRoomId,
        Long senderId,
        String message,
        Boolean isRead
) {
    public static ChatMessageResponse from(ChatMessage chatMessage) {
        return ChatMessageResponse.builder()
                .id(chatMessage.getId())
                .chatRoomId(chatMessage.getChatRoom().getId())
                .senderId(chatMessage.getSender().getId())
                .message(chatMessage.getMessage())
                .isRead(chatMessage.getIsRead())
                .build();
    }
}
