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
        String thumbnailImageUrl,
        String originalImageUrl,
        int unreadCount,
        Status status,
        LocalDateTime createdAt
) {
    public static ChatImageMessageResponse from(ChatImageMessage message) {
        return new ChatImageMessageResponse(
                message.getId(),
                message.getChatRoomId(),
                message.getSenderId(),
                message.getThumbnailImageUrl(),
                message.getOriginalImageUrl(),
                message.getUnreadCount(),
                message.getStatus(),
                message.getCreatedAt()
        );
    }
}
