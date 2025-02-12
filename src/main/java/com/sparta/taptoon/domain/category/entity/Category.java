package com.sparta.taptoon.domain.category.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class Category {
  private Long id;
  private Long user_id;
  private String genre;

  @Builder
  public Category(Long user_id, String genre) {
    this.user_id = user_id;
    this.genre = genre;
  }
}
