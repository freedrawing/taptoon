package com.sparta.taptoon.domain.chat.event;

public class ChatRoomCreatedEvent {
    private final Long chatRoomId;

    public ChatRoomCreatedEvent(Long chatRoomId) {
        this.chatRoomId = chatRoomId;
    }

    public Long getChatRoomId() {
        return chatRoomId;
    }
}