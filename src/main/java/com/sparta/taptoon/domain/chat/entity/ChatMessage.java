package com.sparta.taptoon.domain.chat.entity;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ChatMessage {
    private Long id;
    private Long room_id;
    private Long sender_id;
    private String message;
    private Boolean isRead;
    private Boolean isDeleted;

    @Builder
    public ChatMessage(Long room_id, Long sender_id, String message, Boolean isRead, Boolean isDeleted) {
        this.room_id = room_id;
        this.sender_id = sender_id;
        this.message = message;
        this.isRead = isRead;
        this.isDeleted = isDeleted;
    }
}
