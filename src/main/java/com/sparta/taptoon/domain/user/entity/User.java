package com.sparta.taptoon.domain.user.entity;

import com.sparta.taptoon.domain.user.enums.UserGrade;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Table(name = "taptoon_user")
@Entity
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false, updatable = false)
  private Long id;

  @Column(name = "email", nullable = false)
  private String email;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "nickname", nullable = false)
  private String nickname;

  @Column(name = "password", nullable = false)
  private String password;

  @Column(name = "grade", nullable = false)
  private UserGrade grade;

  @Column(name = "is_deleted", nullable = false)
  private Boolean isDeleted;

  @Builder
  public User(String email, String name, String nickname, String password, UserGrade grade,
      Boolean isDeleted) {
    this.email = email;
    this.name = name;
    this.nickname = nickname;
    this.password = password;
    this.grade = grade;
    this.isDeleted = isDeleted;
  }
}
