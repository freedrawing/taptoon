package com.sparta.taptoon.domain.portfolio.entity;

import com.sparta.taptoon.domain.matchingpost.entity.MatchingPostImage;
import com.sparta.taptoon.domain.member.entity.Member;
import com.sparta.taptoon.domain.portfolio.dto.request.RegisterPortfolioRequest;
import com.sparta.taptoon.domain.portfolio.dto.request.UpdatePortfolioRequest;
import com.sparta.taptoon.global.common.BaseEntity;
import com.sparta.taptoon.global.common.enums.Status;
import com.sparta.taptoon.global.error.exception.NotFoundException;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static com.sparta.taptoon.global.error.enums.ErrorCode.PORTFOLIO_NOT_FOUND;

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
    @JoinColumn(name = "owner_id", nullable = false, updatable = false)
    private Member owner;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", nullable = false, length = 3000, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    // 여기밖에 안 써서 양방향 걱정 안 해도 됨
    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PortfolioFile> portfolioFiles = new ArrayList<>();

    @Builder
    public Portfolio(Member owner, String title, String content) {
        this.owner = owner;
        this.title = title;
        this.content = content;
        status = Status.PENDING;
    }

    // 포트폴리오 내용 수정 (사실상 등록)
    public void registerPortfolio(RegisterPortfolioRequest createRegisterPortfolioRequest) {
        title = createRegisterPortfolioRequest.title();
        content = createRegisterPortfolioRequest.content();
        status = Status.REGISTERED;
        updateCreatedAtToNow(); // 생성시간 정상화
    }

    // 포트폴리오 삭제시 isDeleted 값 true 변경해서 소프트 딜리트
    public void removeMe() {
        status = Status.DELETED;
    }

    public void editMe(UpdatePortfolioRequest request) {
        title = request.title();
        content = request.content();
    }

    public void validateIsDeleted() {
        if (Status.isDeleted(status)) {
            throw new NotFoundException(PORTFOLIO_NOT_FOUND);
        }
    }

    public boolean isMyPortfolio(Long memberId) {
        return owner.getId() == memberId;
    }
}
