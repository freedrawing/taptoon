package com.sparta.taptoon.domain.portfolio.dto.response;

import com.sparta.taptoon.domain.portfolio.entity.Portfolio;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record PortfolioResponse(
        Long memberId,
        Long portfolioId,
        String title,
        String content,
        String fileUrl,
        List<PortfolioImageResponse> portfolioImageResponses,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    // 현재 전체 조회 api에서만 사용중
    public static PortfolioResponse from(Portfolio portfolio) {
        return PortfolioResponse.builder()
                .portfolioId(portfolio.getId())
                .memberId(portfolio.getMember().getId())
                .title(portfolio.getTitle())
                .createdAt(portfolio.getCreatedAt())
                .updatedAt(portfolio.getUpdatedAt())
                .build();
    }

    // 포트폴리오 단건(상세) 조회용으로 따로 분리할지 안할지 고민중
    // PortfolioResponse에 PortfolioImageResponse를 담기
    public static PortfolioResponse from(Portfolio portfolio, List<PortfolioImageResponse> portfolioImageResponses) {
        return PortfolioResponse.builder()
                .portfolioId(portfolio.getId())
                .memberId(portfolio.getMember().getId())
                .title(portfolio.getTitle())
                .content(portfolio.getContent())
                .fileUrl(portfolio.getFileUrl())
                .portfolioImageResponses(portfolioImageResponses)
                .createdAt(portfolio.getCreatedAt())
                .updatedAt(portfolio.getUpdatedAt())
                .build();
    }

}
