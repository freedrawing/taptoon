package com.sparta.taptoon.domain.chat.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "chat_room")
public class ChatRoom {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false, updatable = false)
  private Long id;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "user1_id", nullable = false)
  private Long user1Id;

  @Column(name = "user2_id", nullable = false)
  private Long user2Id;

  @Column(name = "is_deleted", nullable = false)
  private Boolean isDeleted;

  @Builder
  public ChatRoom(String name, Long user1Id, Long user2Id, Boolean isDeleted) {
    this.name = name;
    this.user1Id = user1Id;
    this.user2Id = user2Id;
    this.isDeleted = isDeleted;
  }
}
