package com.sparta.taptoon.domain.portfolio.dto.portfolioDto.response;

import com.sparta.taptoon.domain.member.entity.Member;
import com.sparta.taptoon.domain.portfolio.entity.Portfolio;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record GetPortfolioResponse(Member member, String title, String content, String fileUrl, LocalDateTime createdAt) {

    public static GetPortfolioResponse from(Portfolio portfolio) {
        return GetPortfolioResponse.builder()
                .member(portfolio.getMember())
                .title(portfolio.getTitle())
                .content(portfolio.getContent())
                .fileUrl(portfolio.getFileUrl())
                .createdAt(portfolio.getCreatedAt())
                .build();
    }
}
