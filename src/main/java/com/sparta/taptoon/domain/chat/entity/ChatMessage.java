package com.sparta.taptoon.domain.chat.entity;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;


@Getter
@Document(collection = "chat_message")
public class ChatMessage {

    @Id
    private String id;

    @Field(name = "chat_room_id")
    @Indexed
    private String chatRoomId;

    @Field(name = "sender_id")
    private Long senderId;

    @Field(name = "message")
    private String message;

    @Field(name = "unread_count")
    private int unreadCount;

    @CreatedDate
    @Field(name = "created_at")
    private LocalDateTime createdAt;

    @Field(name = "is_deleted")
    private boolean isDeleted;

    @Builder
    public ChatMessage(String chatRoomId, Long senderId, String message, int unreadCount) {
        this.chatRoomId = chatRoomId;
        this.senderId = senderId;
        this.message = message;
        this.unreadCount = unreadCount;
        this.isDeleted = false;
    }

    public void decrementUnreadCount() {
        if (this.unreadCount > 0) {
            this.unreadCount--;
        }
    }

    public void delete() {
        this.isDeleted = true;
    }
}
