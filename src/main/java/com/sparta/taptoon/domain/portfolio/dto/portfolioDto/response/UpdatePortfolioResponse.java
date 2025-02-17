package com.sparta.taptoon.domain.portfolio.dto.portfolioDto.response;

import com.sparta.taptoon.domain.member.entity.Member;
import com.sparta.taptoon.domain.portfolio.entity.Portfolio;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record UpdatePortfolioResponse(Member member,
                                      String title,
                                      String content,
                                      String fileUrl,
                                      LocalDateTime updatedAt) {

    public static UpdatePortfolioResponse from(Portfolio portfolio) {
        return UpdatePortfolioResponse.builder()
                .member(portfolio.getMember())
                .title(portfolio.getTitle())
                .content(portfolio.getContent())
                .fileUrl(portfolio.getFileUrl())
                .updatedAt(portfolio.getUpdatedAt())
                .build();
    }
}
