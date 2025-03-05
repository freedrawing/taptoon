package com.sparta.taptoon.domain.chat.dto.response;

import com.sparta.taptoon.domain.chat.entity.ChatImageMessage;
import com.sparta.taptoon.global.common.enums.Status;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ChatImageMessageResponse(
        String id,
        String chatRoomId,
        Long senderId,
        String imageUrl,
        Integer unreadCount,
        Status status ,
        LocalDateTime createdAt
) {
    public static ChatImageMessageResponse from(ChatImageMessage chatImageMessage) {
        return ChatImageMessageResponse.builder()
                .id(chatImageMessage.getId())
                .chatRoomId(chatImageMessage.getChatRoomId())
                .senderId(chatImageMessage.getSenderId())
                .imageUrl(chatImageMessage.getImageUrl())
                .unreadCount(chatImageMessage.getUnreadCount())
                .status(Status.PENDING)
                .createdAt(chatImageMessage.getCreatedAt())
                .build();
    }
}
