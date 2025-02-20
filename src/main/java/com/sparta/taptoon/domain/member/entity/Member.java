package com.sparta.taptoon.domain.member.entity;

import com.sparta.taptoon.domain.member.enums.MemberGrade;
import com.sparta.taptoon.domain.member.enums.OAuthProvider;
import com.sparta.taptoon.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Table(name = "member")
@Entity
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "email")
    private String email;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "password")
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "grade", nullable = false)
    private MemberGrade grade;

    @Column(name = "provider_id")
    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider")
    private OAuthProvider provider;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted;

    @Builder
    public Member(String email, String name, String nickname, String password, String providerId, OAuthProvider provider) {
        this.email = email;
        this.name = name;
        this.nickname = nickname;
        this.password = password;
        this.grade = MemberGrade.BASIC;
        this.providerId = providerId;
        this.provider = provider;
        this.isDeleted = false;
    }
    public void setFirstEmail(String email) {this.email = email;}

    public void changePassword(String newPassword) {
        this.password = newPassword;
    }

    public void changeNickname(String newNickname) {
        this.nickname = newNickname;
    }

    public void changeGrade(MemberGrade newGrade) {
        this.grade = newGrade;
    }
}
