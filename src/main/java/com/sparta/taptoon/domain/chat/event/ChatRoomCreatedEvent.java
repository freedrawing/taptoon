package com.sparta.taptoon.domain.chat.event;

public class ChatRoomCreatedEvent {
    private final String chatRoomId;

    public ChatRoomCreatedEvent(String chatRoomId) {
        this.chatRoomId = chatRoomId;
    }

    public String getChatRoomId() {
        return chatRoomId;
    }
}