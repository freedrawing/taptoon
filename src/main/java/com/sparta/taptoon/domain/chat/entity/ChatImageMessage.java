package com.sparta.taptoon.domain.chat.entity;

import com.sparta.taptoon.global.common.enums.Status;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Getter
@Document(collection = "chat_image_message")
public class ChatImageMessage {

    @Id
    private String id;

    @Field(name = "chat_room_id")
    private String chatRoomId;

    @Field(name = "sender_id")
    private Long senderId;

    @Field(name = "thumbnail_image_url")
    private String thumbnailImageUrl;

    @Field(name = "original_image_url")
    private String originalImageUrl;

    @Field(name = "unread_count")
    private int unreadCount;

    @Field(name = "status")
    private Status status;

    @CreatedDate
    @Field(name = "created_at")
    private LocalDateTime createdAt;

    @Field(name = "is_deleted")
    private boolean isDeleted;

    @Builder
    public ChatImageMessage(String chatRoomId, Long senderId, String thumbnailImageUrl, String originalImageUrl, int unreadCount, Status status) {
        if (unreadCount < 0) {
            throw new IllegalArgumentException("읽지 않은 수는 음수가 될 수 없습니다.");
        }
        this.chatRoomId = chatRoomId;
        this.senderId = senderId;
        this.thumbnailImageUrl = thumbnailImageUrl;
        this.originalImageUrl = originalImageUrl;
        this.unreadCount = unreadCount;
        this.status = status;
        this.isDeleted = false;
    }

    public void decrementUnreadCount() {
        if (this.unreadCount > 0) {
            this.unreadCount--;
        }
    }

    public void updateStatus(Status status) {
        this.status = status;
    }

    public void setUnreadCount(int unreadCount) {
        if (unreadCount < 0) {
            throw new IllegalArgumentException("읽지 않은 수는 음수가 될 수 없습니다.");
        }
        this.unreadCount = unreadCount;
    }

    public void delete() {
        this.isDeleted = true;
    }
}