package com.sparta.taptoon.domain.chat.dto.response;

import com.sparta.taptoon.domain.chat.entity.ChatImageMessage;
import com.sparta.taptoon.domain.chat.entity.ChatMessage;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ChatCombinedMessageResponse(
        Long id,
        Long chatRoomId,
        Long senderId,
        String message, // 텍스트 메시지용, 이미지면 null
        String imageUrl, // 이미지 메시지용, 텍스트면 null
        Integer unreadCount,
        String status, // 상태 추가
        String type, // TEXT 또는 IMAGE
        LocalDateTime createdAt // 시간순 정렬용
) {
    public static ChatCombinedMessageResponse from(ChatMessage chatMessage) {
        return ChatCombinedMessageResponse.builder()
                .id(chatMessage.getId())
                .chatRoomId(chatMessage.getChatRoom().getId())
                .senderId(chatMessage.getSender().getId())
                .message(chatMessage.getMessage())
                .unreadCount(chatMessage.getUnreadCount())
                .status("SENT") // ChatMessage는 상태 없으므로 기본값
                .type("TEXT")
                .createdAt(chatMessage.getCreatedAt())
                .build();
    }

    public static ChatCombinedMessageResponse from(ChatImageMessage chatImageMessage) {
        return ChatCombinedMessageResponse.builder()
                .id(chatImageMessage.getId())
                .chatRoomId(chatImageMessage.getChatRoom().getId())
                .senderId(chatImageMessage.getSender().getId())
                .imageUrl(chatImageMessage.getImageUrl())
                .unreadCount(chatImageMessage.getUnreadCount())
                .status(chatImageMessage.getStatus().toString())
                .type("IMAGE")
                .createdAt(chatImageMessage.getCreatedAt())
                .build();
    }
}
