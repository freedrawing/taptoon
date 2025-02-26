package com.sparta.taptoon.domain.chat.dto.response;

import com.sparta.taptoon.domain.chat.entity.ChatImageMessage;
import com.sparta.taptoon.global.common.enums.ImageStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ChatImageMessageResponse(
        Long id,
        Long chatRoomId,
        Long senderId,
        String imageUrl,
        Integer unreadCount,
        ImageStatus status ,
        LocalDateTime createdAt
) {
    public static ChatImageMessageResponse from(ChatImageMessage chatImageMessage) {
        return ChatImageMessageResponse.builder()
                .id(chatImageMessage.getId())
                .chatRoomId(chatImageMessage.getChatRoom().getId())
                .senderId(chatImageMessage.getSender().getId())
                .imageUrl(chatImageMessage.getImageUrl())
                .unreadCount(chatImageMessage.getUnreadCount())
                .status(ImageStatus.PENDING)
                .createdAt(chatImageMessage.getCreatedAt())
                .build();
    }
}
