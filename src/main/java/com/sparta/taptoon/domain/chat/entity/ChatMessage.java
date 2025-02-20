package com.sparta.taptoon.domain.chat.entity;

import com.sparta.taptoon.domain.member.entity.Member;
import com.sparta.taptoon.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "chat_message")
public class ChatMessage extends BaseEntity {

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

    @Column(name = "message", nullable = false)
    private String message;

    @Column(name = "unread_count", nullable = false)
    private int unreadCount;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted;

    @Builder
    public ChatMessage(ChatRoom chatRoom, Member sender, String message, int unreadCount) {
        this.chatRoom = chatRoom;
        this.sender = sender;
        this.message = message;
        this.unreadCount = unreadCount;
        this.isDeleted = false;
    }

    /** 특정 사용자가 메시지를 읽었을 때 호출 */
    public void decrementUnreadCount() {
        if (this.unreadCount > 0) {
            this.unreadCount--;
        }
    }
}
