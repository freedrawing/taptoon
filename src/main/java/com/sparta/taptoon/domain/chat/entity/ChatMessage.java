package com.sparta.taptoon.domain.chat.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "chat_message")
public class ChatMessage {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false, updatable = false)
  private Long id;

  @Column(name = "room_id", nullable = false)
  private Long roomId;

  @Column(name = "sender_id", nullable = false)
  private Long senderId;

  @Column(name = "message", nullable = false)
  private String message;

  @Column(name = "is_read", nullable = false)
  private Boolean isRead;

  @Column(name = "is_deleted", nullable = false)
  private Boolean isDeleted;

  @Builder
  public ChatMessage(Long roomId, Long senderId, String message, Boolean isRead,
                     Boolean isDeleted) {
    this.roomId = roomId;
    this.senderId = senderId;
    this.message = message;
    this.isRead = isRead;
    this.isDeleted = isDeleted;
  }
}
