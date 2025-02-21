package com.sparta.taptoon.domain.chat.dto.response;

import com.sparta.taptoon.domain.chat.entity.ChatRoom;
import lombok.Builder;

@Builder
public record ChatRoomListResponse(
        Long roomId,
        String lastMessage,
        String lastMessageTime,
        int unreadCount,
        int memberCount
) {
    public static ChatRoomListResponse of(ChatRoom chatRoom, String lastMessage, String lastMessageTime, int unreadCount) {
        return ChatRoomListResponse.builder()
                .roomId(chatRoom.getId())
                .lastMessage(lastMessage)
                .lastMessageTime(lastMessageTime)
                .unreadCount(unreadCount)
                .memberCount(chatRoom.getMemberCount())
                .build();
    }
}
