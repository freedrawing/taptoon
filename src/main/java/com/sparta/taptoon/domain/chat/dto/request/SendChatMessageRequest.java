package com.sparta.taptoon.domain.chat.dto.request;

import com.sparta.taptoon.domain.chat.entity.ChatMessage;
import com.sparta.taptoon.domain.chat.entity.ChatRoom;
import com.sparta.taptoon.domain.member.entity.Member;

public record SendChatMessageRequest(String message) {

    public ChatMessage toEntity(ChatRoom chatRoom, Member sender) {
        return ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(sender)
                .message(this.message)
                .unreadCount(chatRoom.getMemberCount() - 1) // 채팅방 인원 - 1(보낸 사람 제외)
                .build();
    }
}
