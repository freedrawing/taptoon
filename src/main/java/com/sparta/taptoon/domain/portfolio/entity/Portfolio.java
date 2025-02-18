package com.sparta.taptoon.domain.portfolio.entity;

import com.sparta.taptoon.domain.member.entity.Member;
import com.sparta.taptoon.domain.portfolio.dto.request.PortfolioRequest;
import com.sparta.taptoon.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "portfolios")
public class Portfolio extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, updatable = false)
    private Member member;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", nullable = false, length = 3000, columnDefinition = "TEXT")
    private String content;

    @Column(name = "file_url", nullable = false)
    private String fileUrl;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    @Builder
    public Portfolio(Member member, String title, String content, String fileUrl) {
        this.member = member;
        this.title = title;
        this.content = content;
        this.fileUrl = fileUrl;
        this.isDeleted = false;
    }

    // 포트폴리오 수정 메서드 request 값
    public void update(PortfolioRequest portfolioRequest) {
        this.title = portfolioRequest.title();
        this.content = portfolioRequest.content();
        this.fileUrl = portfolioRequest.fileUrl();
    }

    // 포트폴리오 삭제시 isDeleted 값 true 변경해서 소프트 딜리트
    public void remove() {
        this.isDeleted = true;
    }
}
