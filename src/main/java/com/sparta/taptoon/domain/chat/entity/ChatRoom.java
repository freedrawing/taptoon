package com.sparta.taptoon.domain.chat.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChatRoom {
  private Long id;
  private String name;
  private Long user1_id;
  private Long user2_id;
  private Boolean is_deleted;

  @Builder
  public ChatRoom(String name, Long user1_id, Long user2_id, Boolean is_deleted) {
    this.name = name;
    this.user1_id = user1_id;
    this.user2_id = user2_id;
    this.is_deleted = is_deleted;
  }
}
