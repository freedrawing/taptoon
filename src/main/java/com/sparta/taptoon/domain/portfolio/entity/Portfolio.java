package com.sparta.taptoon.domain.portfolio.entity;

import com.sparta.taptoon.domain.member.entity.Member;
import com.sparta.taptoon.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "portfolio")
public class Portfolio extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private Member member;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "file_url", nullable = false)
    private String fileUrl;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    @Builder
    public Portfolio(Member member, String content, String fileUrl, boolean isDeleted) {
        this.member = member;
        this.content = content;
        this.fileUrl = fileUrl;
        this.isDeleted = isDeleted;
    }
}
