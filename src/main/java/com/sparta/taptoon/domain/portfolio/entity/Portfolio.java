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
@Table(name = "portfolio")
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

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    @Builder
    public Portfolio(Member member, String title, String content) {
        this.member = member;
        this.title = title;
        this.content = content;
        this.isDeleted = false;
    }

    // 포트폴리오 내용 등록 및 수정 request 값
    public void updatePortfolio(PortfolioRequest createPortfolioRequest) {
        this.title = createPortfolioRequest.title();
        this.content = createPortfolioRequest.content();
    }

    // 포트폴리오 삭제시 isDeleted 값 true 변경해서 소프트 딜리트
    public void remove() {
        this.isDeleted = true;
    }
}
