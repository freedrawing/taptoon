package com.sparta.taptoon.domain.auth.entity;

import com.sparta.taptoon.domain.member.entity.Member;
import com.sparta.taptoon.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "refresh_token")
public class RefreshToken extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token", nullable = false)
    private String token;

    @JoinColumn(name = "member_id", nullable = false, updatable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @Column(name = "device_info")
    private String deviceInfo;

    @Column(name = "expires_at",nullable = false)
    private LocalDateTime expiresAt;

    @Builder
    public RefreshToken(String token, Member member, String deviceInfo, LocalDateTime expiresAt) {
        this.token = token;
        this.member = member;
        this.deviceInfo = deviceInfo;
        this.expiresAt = expiresAt;
    }
}
