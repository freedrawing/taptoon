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
        List<PortfolioImageResponse> files,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    // PortfolioResponse에 PortfolioImageResponse를 담기
    public static PortfolioResponse from(Portfolio portfolio, List<PortfolioImageResponse> portfolioImageResponses) {
        return PortfolioResponse.builder()
                .portfolioId(portfolio.getId())
                .memberId(portfolio.getMember().getId())
                .title(portfolio.getTitle())
                .content(portfolio.getContent())
                .files(portfolioImageResponses)
                .createdAt(portfolio.getCreatedAt())
                .updatedAt(portfolio.getUpdatedAt())
                .build();
    }
}
