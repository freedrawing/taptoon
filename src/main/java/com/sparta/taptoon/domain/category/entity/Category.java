package com.sparta.taptoon.domain.category.entity;

import com.sparta.taptoon.domain.category.enums.Genre;
import com.sparta.taptoon.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Table(name = "category")
@Entity
public class Category {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false, updatable = false)
  private Long id;

  @JoinColumn(name = "member_id", nullable = false, updatable = false)
  @ManyToOne(fetch = FetchType.LAZY)
  private Member member;

  @Enumerated(EnumType.STRING)
  @Column(name = "genre", nullable = false)
  private Genre genre;

  @Builder
  public Category(Member member, Genre genre) {
    this.member = member;
    this.genre = genre;
  }
}
