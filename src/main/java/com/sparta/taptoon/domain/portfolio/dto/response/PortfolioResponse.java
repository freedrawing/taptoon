package com.sparta.taptoon.domain.portfolio.dto.response;

import com.sparta.taptoon.domain.member.entity.Member;
import com.sparta.taptoon.domain.portfolio.entity.Portfolio;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record PortfolioResponse(
        Long portfolioId,
        Member member,
        String title,
        String content,
        String fileUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static PortfolioResponse from(Portfolio portfolio) {
        return PortfolioResponse.builder()
                .portfolioId(portfolio.getId())
                .member(portfolio.getMember())
                .title(portfolio.getTitle())
                .content(portfolio.getContent())
                .fileUrl(portfolio.getFileUrl())
                .createdAt(portfolio.getCreatedAt())
                .build();
    }
}
