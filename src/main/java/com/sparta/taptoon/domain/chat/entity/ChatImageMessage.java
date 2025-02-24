package com.sparta.taptoon.domain.chat.entity;

import com.sparta.taptoon.domain.member.entity.Member;
import com.sparta.taptoon.global.common.BaseEntity;
import com.sparta.taptoon.global.common.enums.ImageStatus;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "chat_image_message")
public class ChatImageMessage extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private Member sender;

    @Column(name = "image_url", nullable = false, length = 1000)
    private String imageUrl;

    @Column(name = "unread_count", nullable = false)
    private int unreadCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ImageStatus status;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted;

    @Builder
    public ChatImageMessage(ChatRoom chatRoom, Member sender, String imageUrl, int unreadCount, ImageStatus status) {
        this.chatRoom = chatRoom;
        this.sender = sender;
        this.imageUrl = imageUrl;
        this.unreadCount = unreadCount;
        this.status = status;
        this.isDeleted = false;
    }

    /** 특정 사용자가 메시지를 읽었을 때 호출 */
    public void decrementUnreadCount() {
        if (this.unreadCount > 0) {
            this.unreadCount--;
        }
    }

    public void updateStatus(ImageStatus status) {
        this.status = status;
    }

    public void setUnreadCount(int unreadCount) {
        if (unreadCount < 0) {
            throw new IllegalArgumentException("읽지 않은 수는 음수가 될 수 없습니다.");
        }
        this.unreadCount = unreadCount;
    }

}
